# Eclipse Java 템플릿 설치 툴

Eclipse 작업공간에 Java 템플릿 81개와 F9 단축키를 등록하는 오프라인 설치 도구입니다. 설치 전 설정 백업과 복원을 지원합니다.

## Windows에서 설치

1. `Windows-Eclipse-Java-Templates-81.zip`을 다운로드해 압축을 풉니다.
2. Java 17 이상을 설치하고 Eclipse를 종료합니다.
3. `install.cmd`를 실행하고 Eclipse 작업공간 경로를 입력합니다.
4. Eclipse Java 편집기에서 `F9`를 누릅니다.

복원은 Eclipse 종료 후 `restore.cmd`로 실행합니다. 상세 동작과 주의사항은 [한글 안내](README_KO.txt)를 참고하세요.

## 구성

- 문법 템플릿 55개와 프로그램 전체 템플릿 26개
- 기존 사용자 템플릿과 관련 없는 단축키 보존
- 재설치 중복 방지, 설치 직전 환경설정 백업, 백업 복원
- `InstallTemplates.java`: 확인 가능한 설치 프로그램 소스
- `installer.jar`: Java 17 이상에서 실행하는 설치 프로그램

## 검증 및 소스 빌드

```sh
python3 tests/test_installer.py
javac --release 17 -encoding UTF-8 -d build InstallTemplates.java
jar --create --file installer.jar --main-class InstallTemplates -C build .
```

자동 검증은 임시 작업공간에서 실행됩니다. 설치·중복 방지·설정 보존·복원·잘못된 경로 거부를 확인합니다. Windows CMD 실행 파일 자체는 Windows에서 직접 검증하지 않았습니다.


## 라이선스

이 프로젝트의 자체 소스 코드와 문서는 [MIT License](LICENSE)로 공개합니다. 외부 라이브러리·데이터·미디어에는 각 출처의 조건이 적용됩니다. [외부 자료 안내](THIRD_PARTY_NOTICES.md)를 참고하세요.
