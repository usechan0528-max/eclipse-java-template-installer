from pathlib import Path
import subprocess,xml.etree.ElementTree as E,re,json,tempfile,os
root=Path(__file__).resolve().parents[1]
java=os.environ.get('JAVA', 'java')
jar=root/'installer.jar'
base=Path(tempfile.mkdtemp(prefix='eclipse-template-test-'))
reports=[]
def run(ws,*options):
 p=subprocess.run([java,'-jar',str(jar),*options,str(ws)],capture_output=True,text=True)
 assert p.returncode==0,p.stderr+p.stdout
 return p.stdout
def prefs(ws):return ws/'.metadata/.plugins/org.eclipse.core.runtime/.settings'
def prop_xml(p,key):
 l=next(l.split('=',1)[1] for l in p.read_text(encoding='latin1').splitlines() if l.startswith(key+'='))
 def un(m):
  t=m[1];return chr(int(t[1:],16)) if t.startswith('u') else {'n':'\n','r':'\r','t':'\t','f':'\f'}.get(t,t)
 return E.fromstring(re.sub(r'\\(u[0-9a-fA-F]{4}|.)',un,l))
ws=base/'새 작업공간';(ws/'.metadata').mkdir(parents=True,exist_ok=True)
run(ws)
p=prefs(ws);r=prop_xml(p/'org.eclipse.jdt.ui.prefs','org.eclipse.jdt.ui.text.custom_templates')
assert len(r)==81 and len({e.get('name') for e in r})==81
assert all(e.get('context')=='java' for e in r)
source=E.parse(root/'templates.xml').getroot()
assert {e.get('name'):e.text for e in r}=={e.get('name'):e.text for e in source}
reports.append('PASS: fresh workspace installed all 81 templates with Korean comments and all original code intact.')
run(ws)
r=prop_xml(p/'org.eclipse.jdt.ui.prefs','org.eclipse.jdt.ui.text.custom_templates');assert len(r)==81
reports.append('PASS: repeat installation did not create duplicates.')
ws=base/'existing workspace';p=prefs(ws);p.mkdir(parents=True,exist_ok=True)
custom='<templates><template name="personal" context="java" enabled="true">personal();</template><template name="jfor" context="java" enabled="true">old();</template><template name="jfor" context="java" enabled="true">duplicate();</template></templates>'
cmd='<org.eclipse.ui.commands><keyBinding commandId="my.personal.command" contextId="org.eclipse.ui.contexts.window" keyConfigurationId="org.eclipse.ui.defaultAcceleratorConfiguration" keySequence="CTRL+9"/><keyBinding commandId="old.f9" contextId="org.eclipse.ui.contexts.window" keyConfigurationId="org.eclipse.ui.defaultAcceleratorConfiguration" keySequence="F9"/></org.eclipse.ui.commands>'
originals={'org.eclipse.jdt.ui.prefs':('eclipse.preferences.version=1\nmy.existing.setting=keep-this\norg.eclipse.jdt.ui.text.custom_templates='+custom+'\n').encode(),'org.eclipse.ui.workbench.prefs':('eclipse.preferences.version=1\nmy.other.setting=keep-that\norg.eclipse.ui.commands='+cmd+'\n').encode()}
for n,b in originals.items():(p/n).write_bytes(b)
run(ws)
r=prop_xml(p/'org.eclipse.jdt.ui.prefs','org.eclipse.jdt.ui.text.custom_templates');assert len(r)==82
assert len([e for e in r if e.get('name')=='jfor'])==1
assert any(e.get('name')=='personal' and e.text=='personal();' for e in r)
assert 'my.existing.setting=keep-this' in (p/'org.eclipse.jdt.ui.prefs').read_text()
c=prop_xml(p/'org.eclipse.ui.workbench.prefs','org.eclipse.ui.commands')
assert any(e.get('commandId')=='my.personal.command' for e in c)
f9=[e for e in c if e.get('keySequence')=='F9'];assert len(f9)==1
assert f9[0].get('commandId')=='org.eclipse.jdt.ui.specific_content_assist.command'
assert any(e.get('value')=='org.eclipse.jdt.ui.templateProposalCategory' for e in f9[0])
reports.append('PASS: other templates, settings and unrelated key bindings preserved; duplicate package names replaced; F9 mapped correctly.')
run(ws,'--restore')
for n,b in originals.items():assert (p/n).read_bytes()==b
reports.append('PASS: restore returned both preference files byte-for-byte to their original contents.')
ws=base/'missing-original-files';(ws/'.metadata').mkdir(parents=True,exist_ok=True)
run(ws);run(ws,'--restore')
assert not (prefs(ws)/'org.eclipse.jdt.ui.prefs').exists() and not (prefs(ws)/'org.eclipse.ui.workbench.prefs').exists()
reports.append('PASS: restore removes installer-created preference files when originals did not exist.')
bad=base/'not-a-workspace';bad.mkdir(exist_ok=True)
p=subprocess.run([java,'-jar',str(jar),str(bad)],capture_output=True,text=True)
assert p.returncode!=0 and not (bad/'.metadata').exists()
reports.append('PASS: invalid workspace rejected without creating settings.')
(base/'verification.txt').write_text('\n'.join(reports)+'\n')
print('\n'.join(reports))
