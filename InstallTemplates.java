import java.io.*;
import java.nio.channels.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javax.xml.XMLConstants;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

/** Offline, reversible installer for Eclipse workspace preferences. Requires Java 17+. */
public class InstallTemplates {
    static final String TEMPLATE_KEY = "org.eclipse.jdt.ui.text.custom_templates";
    static final String COMMAND_KEY = "org.eclipse.ui.commands";
    static final String COMMAND = "org.eclipse.jdt.ui.specific_content_assist.command";
    static final String PARAM = "org.eclipse.jdt.ui.specific_content_assist.category_id";
    static final String CATEGORY = "org.eclipse.jdt.ui.templateProposalCategory";
    static final String WINDOW = "org.eclipse.ui.contexts.window";
    static final String CONFIG = "org.eclipse.ui.defaultAcceleratorConfiguration";
    static final List<String> FILES = List.of("org.eclipse.jdt.ui.prefs", "org.eclipse.ui.workbench.prefs");

    public static void main(String[] args) {
        try { run(args); }
        catch (Exception e) { System.err.println("FAILED: " + e.getMessage()); System.exit(1); }
    }
    static void run(String[] args) throws Exception {
        boolean restore = args.length > 0 && args[0].equals("--restore");
        int index = restore ? 1 : 0;
        String workspace;
        if (args.length > index) workspace = args[index];
        else {
            System.out.println("Close Eclipse before continuing.");
            System.out.println("Enter the Eclipse WORKSPACE folder (not the Eclipse application folder).");
            System.out.print("Workspace path: ");
            workspace = new BufferedReader(new InputStreamReader(System.in)).readLine();
        }
        if (workspace == null || workspace.isBlank()) throw new IOException("Workspace path is required.");
        workspace = workspace.trim();
        if (workspace.startsWith("\"") && workspace.endsWith("\"")) workspace = workspace.substring(1, workspace.length()-1);
        Path ws = Path.of(workspace).toAbsolutePath().normalize();
        Path metadata = ws.resolve(".metadata");
        if (!Files.isDirectory(metadata)) throw new IOException("No .metadata folder here. Open this workspace in Eclipse once, close Eclipse, then try again.");
        if (System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("windows")) {
            boolean active = ProcessHandle.allProcesses().anyMatch(p -> p.info().command().map(c -> {
                String name = Path.of(c).getFileName().toString().toLowerCase(Locale.ROOT);
                return name.equals("eclipse.exe") || name.equals("eclipsec.exe");
            }).orElse(false));
            if (active) throw new IOException("Eclipse is running. Close all Eclipse windows and retry.");
        }
        // Eclipse holds this workspace lock while it is running.
        try (FileChannel ch = FileChannel.open(metadata.resolve(".lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            FileLock lock;
            try { lock = ch.tryLock(); } catch (OverlappingFileLockException e) { lock = null; }
            if (lock == null) throw new IOException("Workspace is locked. Close Eclipse and retry.");
            try (FileLock held = lock) {
                Path prefs = metadata.resolve(".plugins/org.eclipse.core.runtime/.settings");
                if (restore) restoreLatest(ws, prefs);
                else install(ws, prefs);
            }
        }
    }
    static Document xml(String text) throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        f.setFeature("http://xml.org/sax/features/external-general-entities", false);
        f.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        f.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        f.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return f.newDocumentBuilder().parse(new InputSource(new StringReader(text)));
    }
    static String serialize(Document d) throws Exception {
        TransformerFactory f = TransformerFactory.newInstance();
        f.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        f.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
        Transformer t = f.newTransformer();
        t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        t.setOutputProperty(OutputKeys.INDENT, "no");
        StringWriter out = new StringWriter(); t.transform(new DOMSource(d), new StreamResult(out)); return out.toString();
    }
    static Properties load(Path p) throws IOException {
        Properties props = new Properties();
        if (Files.exists(p)) try (InputStream in = Files.newInputStream(p)) { props.load(in); }
        return props;
    }
    static byte[] bytes(Properties p) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        p.store(out, "Eclipse Java course templates - backup available in .codex-template-backups");
        return out.toByteArray();
    }
    static List<Element> elements(Document d, String tag) {
        List<Element> out = new ArrayList<>();
        NodeList list = d.getDocumentElement().getElementsByTagName(tag);
        for (int i=0;i<list.getLength();i++) out.add((Element) list.item(i));
        return out;
    }
    static void install(Path ws, Path prefs) throws Exception {
        Path packageDir = Path.of(InstallTemplates.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toAbsolutePath();
        if (!Files.isDirectory(packageDir)) packageDir = packageDir.getParent();
        Document incoming = xml(Files.readString(packageDir.resolve("templates.xml")));
        if (!incoming.getDocumentElement().getTagName().equals("templates")) throw new IOException("Invalid templates.xml root.");
        List<Element> additions = elements(incoming, "template");
        Set<String> names = new HashSet<>();
        for (Element e : additions) {
            if (!names.add(e.getAttribute("name"))) throw new IOException("Duplicate package template: " + e.getAttribute("name"));
            if (!e.getAttribute("context").equals("java")) throw new IOException("Unexpected template context.");
        }
        if (names.size()!=81) throw new IOException("Expected exactly 81 package templates.");
        Properties javaPrefs = load(prefs.resolve(FILES.get(0)));
        Document templates = xml(javaPrefs.getProperty(TEMPLATE_KEY, "<templates/>"));
        for (Element e : elements(templates, "template")) {
            if (names.contains(e.getAttribute("name"))) e.getParentNode().removeChild(e);
        }
        for (Element e : additions) templates.getDocumentElement().appendChild(templates.importNode(e,true));
        javaPrefs.setProperty("eclipse.preferences.version", "1");
        javaPrefs.setProperty(TEMPLATE_KEY, serialize(templates));
        Properties uiPrefs = load(prefs.resolve(FILES.get(1)));
        Document commands = xml(uiPrefs.getProperty(COMMAND_KEY, "<org.eclipse.ui.commands/>"));
        for (Element e : elements(commands, "keyBinding")) {
            boolean thisCommand = COMMAND.equals(e.getAttribute("commandId")) && elementsUnder(e, "parameter").stream().anyMatch(p -> PARAM.equals(p.getAttribute("id")) && CATEGORY.equals(p.getAttribute("value")));
            boolean sameGlobalF9 = "F9".equalsIgnoreCase(e.getAttribute("keySequence")) && WINDOW.equals(e.getAttribute("contextId")) && CONFIG.equals(e.getAttribute("keyConfigurationId"));
            if (thisCommand || sameGlobalF9) e.getParentNode().removeChild(e);
        }
        Element binding = commands.createElement("keyBinding");
        binding.setAttribute("commandId", COMMAND); binding.setAttribute("contextId", WINDOW);
        binding.setAttribute("keyConfigurationId", CONFIG); binding.setAttribute("keySequence", "F9");
        Element parameter = commands.createElement("parameter"); parameter.setAttribute("id", PARAM); parameter.setAttribute("value", CATEGORY);
        binding.appendChild(parameter); commands.getDocumentElement().appendChild(binding);
        uiPrefs.setProperty("eclipse.preferences.version", "1"); uiPrefs.setProperty(COMMAND_KEY, serialize(commands));
        Files.createDirectories(prefs);
        Path backup = ws.resolve(".codex-template-backups").resolve(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS"))+"-"+UUID.randomUUID().toString().substring(0,8));
        Files.createDirectories(backup);
        for (String name : FILES) {
            Path original = prefs.resolve(name);
            if (Files.exists(original)) Files.copy(original, backup.resolve(name));
            else Files.writeString(backup.resolve(name+".missing"), "This preference file did not exist before installation.\n");
        }
        try {
            atomic(prefs.resolve(FILES.get(0)), bytes(javaPrefs));
            atomic(prefs.resolve(FILES.get(1)), bytes(uiPrefs));
            Document check = xml(load(prefs.resolve(FILES.get(0))).getProperty(TEMPLATE_KEY));
            for (String name : names) if (elements(check,"template").stream().filter(e -> name.equals(e.getAttribute("name"))).count()!=1) throw new IOException("Verification failed: "+name);
        } catch (Exception e) { restoreFrom(backup,prefs); throw e; }
        System.out.println("SUCCESS: 55 syntax + 26 whole-program templates installed. Shortcut: F9.");
        System.out.println("Existing templates with other names and unrelated settings were preserved.");
        System.out.println("Backup: " + backup);
        System.out.println("Start Eclipse with this workspace. Press F9 on a blank code position to see all templates.");
    }
    static List<Element> elementsUnder(Element root, String tag) {
        List<Element> out=new ArrayList<>(); NodeList list=root.getElementsByTagName(tag);
        for(int i=0;i<list.getLength();i++)out.add((Element)list.item(i)); return out;
    }
    static void atomic(Path target, byte[] data) throws IOException {
        Files.createDirectories(target.getParent()); Path tmp=Files.createTempFile(target.getParent(),"codex-templates-", ".tmp");
        try {
            Files.write(tmp,data);
            try { Files.move(tmp,target,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE); }
            catch (AtomicMoveNotSupportedException e) { Files.move(tmp,target,StandardCopyOption.REPLACE_EXISTING); }
        } finally { Files.deleteIfExists(tmp); }
    }
    static void restoreLatest(Path ws, Path prefs) throws Exception {
        Path root=ws.resolve(".codex-template-backups");
        if(!Files.isDirectory(root))throw new IOException("No installer backup found in this workspace.");
        Path latest;
        try(var stream=Files.list(root)) { latest=stream.filter(Files::isDirectory).filter(p -> FILES.stream().allMatch(n -> Files.exists(p.resolve(n))||Files.exists(p.resolve(n+".missing")))).max(Comparator.comparing(p -> p.getFileName().toString())).orElseThrow(() -> new IOException("No complete backup found.")); }
        restoreFrom(latest,prefs); System.out.println("RESTORED settings from: "+latest);
        System.out.println("The backup remains available. Repeated restore uses the same latest backup.");
    }
    static void restoreFrom(Path backup, Path prefs) throws IOException {
        for(String name:FILES) {
            if(Files.exists(backup.resolve(name)))atomic(prefs.resolve(name),Files.readAllBytes(backup.resolve(name)));
            else if(Files.exists(backup.resolve(name+".missing")))Files.deleteIfExists(prefs.resolve(name));
            else throw new IOException("Incomplete backup: "+name);
        }
    }
}
