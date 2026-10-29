Windows용 Eclipse Java 템플릿 설치 패키지

포함 내용
- 기본 문법 템플릿 55개 + 유사 문제용 롱 템플릿 26개 = 총 81개
- F9: Content Assist (Template Proposals) 목록
- 한글 용도 / 문제 표현 / 올바른 삽입 위치 / 수정할 곳 주석
- 다른 이름의 사용자 템플릿과 관련 없는 단축키 설정 보존

준비
1. Java 17 이상의 JDK가 설치되어 있어야 합니다. JAVA_HOME 또는 PATH로 java.exe가 실행되어야 합니다.
   Eclipse가 실행되더라도 Eclipse 전용 내장 Java만 있으면 설치 파일에서 Java를 못 찾을 수 있습니다.
   이 경우 명령 프롬프트에서 다음처럼 JDK의 java.exe를 직접 지정합니다.
   "C:\실제JDK폴더\bin\java.exe" -jar "C:\압축푼폴더\installer.jar"
2. Windows에서 Eclipse를 한 번 실행해 작업공간을 만들고, 모든 Eclipse 창을 닫으세요.
3. ZIP 파일을 완전히 압축 해제하세요. ZIP 내부에서 바로 실행하지 마세요.

설치
1. install.cmd를 두 번 클릭합니다. 관리자 권한은 필요하지 않습니다.
2. Workspace path: 질문에 실제 작업공간 경로를 입력하고 Enter를 누릅니다.
   예: C:\Users\사용자이름\eclipse-workspace
   입력할 폴더 안에 .metadata 폴더가 있어야 합니다. 숨김 폴더일 수 있습니다.
   Eclipse 프로그램이 설치된 폴더를 입력하는 것이 아닙니다.
3. SUCCESS 메시지가 나오면 설치 완료입니다.
4. 해당 작업공간으로 Eclipse를 다시 실행합니다.

다른 방법: 명령 프롬프트에서
  install.cmd "C:\Users\사용자이름\eclipse-workspace"
  또는 java -jar installer.jar "C:\Users\사용자이름\eclipse-workspace"

사용
- Java 편집기에서 약어가 없는 빈 코드 위치에 커서를 놓고 F9를 누르면 81개가 모두 목록에 나옵니다.
- 노트북의 기능 키 설정에 따라 Fn+F9가 필요할 수 있습니다.
- 이미 약어를 입력했다면 해당 약어로 목록이 좁혀집니다.
- j...: 짧은 문법 틀. 예: jscan, jfor, jctor.
- oj...: 프로그램 전체. 예: ojorder, ojcalculator, ojgrades, ojstockmap.
- 롱 템플릿은 Main.java 전체 교체용입니다. 기존 코드를 백업하고 파일을 비운 뒤 삽입하세요.
- 목록은 Java 코드 위치와 관계없이 표시되지만, 실제 삽입은 // 삽입 위치: 주석을 따라야 합니다.
- 주석·문자열을 편집 중일 때는 Java 코드의 빈 위치로 커서를 옮겨 목록을 여세요.
- 롱 26개 용도는 long-template-guide.txt, 문법 55개 안내는 syntax-template-guide.txt를 참고하세요.

백업·복원
- 작업공간의 .codex-template-backups 폴더에 설치 직전 설정 두 파일을 백업합니다.
- 같은 작업공간에 재설치해도 이 패키지의 템플릿 이름은 중복되지 않습니다.
- F9의 기존 전역 사용자 바인딩과 이 템플릿 명령의 이전 사용자 바인딩은 F9로 갱신됩니다.
- 복원하려면 Eclipse를 닫고 restore.cmd를 실행하여 같은 작업공간을 입력하세요.
- 복원은 가장 최근 설치 직전의 두 환경설정 파일을 그대로 돌립니다.
  따라서 설치 후 바꾼 Java UI·단축키 설정도 그 시점으로 되돌아갑니다.
- 설치 중 저장 오류가 발생하면 자동으로 해당 백업으로 되돌립니다.

수동 설치 (자동 설치를 사용하지 않을 때)
1. Eclipse > Window > Preferences > Java > Editor > Templates > Import...
2. 이 폴더의 templates.xml을 선택하고 Apply and Close.
3. General > Keys에서 Content Assist (Template Proposals)를 검색합니다.
4. Binding: F9 / When: In Windows로 지정하고 충돌 여부를 확인한 뒤 Apply and Close.
5. 수동 Import는 같은 이름이 중복될 수 있습니다. 기존 등록이 있으면 해당 항목을 먼저 제거하세요.

파일 설명
- install.cmd: Windows 설치 시작 파일
- restore.cmd: 가장 최근 설치 전 설정으로 복원
- installer.jar: Java 17 이상에서 실행되는 설치 프로그램
- InstallTemplates.java: 설치 프로그램의 확인 가능한 소스 코드
- templates.xml: Eclipse용 81개 템플릿
- long-template-guide.txt / syntax-template-guide.txt: 한글 용도·사용법

이 패키지는 Eclipse/JDK 자체를 설치하지 않습니다.
OJ에 제출하는 파일은 템플릿에서 만든 Main.java이며, 설치 파일은 제출하지 않습니다.
