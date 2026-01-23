# 통합 협업 + AI 작성 도우미 게시판 플랫폼 - Frontend

React + TypeScript + Vite 기반 통합 협업 플랫폼 프론트엔드입니다.
**게시판 시스템**과 **AI 활용**, **칸반 보드**를 하나의 플랫폼에 통합하여 팀 협업 효율을 극대화하여 게시글을 올릴 수 있습니다.

<img 
  src="https://github.com/FAITRUEE/board_web/blob/main/Animation.gif?raw=true"
  width="600"
/>

---

## 🎯 핵심 기능

### 📝 게시판 시스템
- **JWT 인증**: 안전한 회원가입/로그인
- **게시글 CRUD**: 작성/수정/삭제 (작성자만)
- **카테고리 시스템**: 색상/아이콘 커스터마이징
- **태그 시스템**: 게시글 분류 및 검색
- **파일 업로드**: 이미지 미리보기, 다중 파일 지원
- **그림 그리기**: 캔버스로 직접 그림 첨부
- **비밀글**: 비밀번호로 보호
- **Rich Text Editor**: WYSIWYG 에디터 (Quill.js)
- **AI 작성 도우미**: Ollama 연동 (선택사항)
- **댓글 시스템**: 실시간 댓글 작성/수정/삭제
- **좋아요**: 사용자당 게시글당 1회
- **페이지네이션**: 10개씩 페이징
- **정렬**: 최신순/오래된순/조회순/좋아요순
- **검색**: 제목/내용 실시간 검색

### 📊 칸반 보드 시스템 ⭐
- **팀 기반 작업 관리**: 팀별 독립적인 워크스페이스
- **드래그 앤 드롭**: @dnd-kit 활용한 직관적인 카드 이동
- **카드 관리**: TODO/IN_PROGRESS/DONE 상태 관리
- **우선순위**: LOW/MEDIUM/HIGH/URGENT 4단계
- **체크리스트**: 작업 세부 사항 추적
- **담당자 지정**: 팀원 할당
- **마감일 관리**: 일정 추적 및 상태 표시
- **태그 시스템**: 카드 분류
- **실시간 동기화**: React Query 낙관적 업데이트
- **상세 모달**: 풍부한 카드 정보 편집

---

## 🛠️ 기술 스택

### Core
- **React** 18.x - UI 라이브러리
- **TypeScript** 5.x - 타입 안정성
- **Vite** 5.x - 빌드 도구

### 상태 관리
- **TanStack Query (React Query)** - 서버 상태 관리
- **React Context** - 전역 상태 (인증)

### 스타일링
- **Tailwind CSS** 3.x - 유틸리티 CSS
- **Shadcn/ui** - UI 컴포넌트 라이브러리
- **Lucide React** - 아이콘

### 주요 라이브러리
- **React Router v6** - 라우팅
- **@dnd-kit** - 드래그 앤 드롭
- **Quill.js** - Rich Text Editor
- **date-fns** - 날짜 처리

---

## 📁 프로젝트 구조
```
src/
├── components/           # 재사용 컴포넌트
│   ├── board/           # 게시판 관련
│   │   ├── CommentList.tsx
│   │   ├── CategorySelect.tsx
│   │   ├── TagInput.tsx
│   │   ├── RichTextEditor.tsx
│   │   ├── DrawingCanvas.tsx
│   │   ├── AIWritingAssistant.tsx
│   │   └── SecretPasswordDialog.tsx
│   ├── kanban/          # 칸반 관련 ⭐
│   │   ├── KanbanBoard.tsx
│   │   ├── KanbanColumn.tsx
│   │   ├── KanbanCard.tsx
│   │   ├── CardModal.tsx
│   │   └── CardDetailModal.tsx
│   ├── layout/          # 레이아웃
│   │   ├── Layout.tsx
│   │   └── Header.tsx
│   └── ui/              # Shadcn UI 컴포넌트
│
├── pages/               # 페이지 컴포넌트
│   ├── Auth.tsx                    # 로그인/회원가입
│   ├── PostListPage.tsx            # 게시글 목록
│   ├── PostDetailPage.tsx          # 게시글 상세
│   ├── PostCreatePage.tsx          # 게시글 작성
│   ├── PostEditPage.tsx            # 게시글 수정
│   ├── CategoryManagePage.tsx      # 카테고리 관리
│   ├── TeamList.tsx                # 팀 목록 ⭐
│   ├── KanbanList.tsx              # 칸반 보드 목록 ⭐
│   ├── KanbanBoard.tsx             # 칸반 보드 상세 ⭐
│   └── NotFound.tsx                # 404
│
├── hooks/               # Custom Hooks
│   ├── usePosts.ts              # 게시글 관련
│   ├── useComments.ts           # 댓글 관련
│   ├── useCategories.ts         # 카테고리 관련
│   ├── useTags.ts               # 태그 관련
│   ├── useTeam.ts               # 팀 관련 ⭐
│   └── useKanban.ts             # 칸반 관련 ⭐
│
├── services/            # API 서비스
│   ├── authService.ts           # 인증 API
│   ├── postService.ts           # 게시글 API
│   ├── commentService.ts        # 댓글 API
│   ├── categoryService.ts       # 카테고리 API
│   └── tagService.ts            # 태그 API
│
├── contexts/            # React Context
│   └── AuthContext.tsx          # 인증 상태
│
├── types/               # TypeScript 타입
│   ├── auth.ts
│   ├── post.ts
│   ├── comment.ts
│   ├── category.ts
│   ├── tag.ts
│   ├── team.ts          # ⭐
│   └── kanban.ts        # ⭐
│
├── utils/               # 유틸리티
│   └── SecurityUtil.ts          # ⭐
│
├── App.tsx              # 앱 엔트리
└── main.tsx             # Vite 엔트리
```

---

## 🚀 시작하기

### 필수 요구사항
- **Node.js** 18.x 이상
- **npm** 또는 **yarn**
- 백엔드 API 서버 실행 필요

### 설치 및 실행
```bash
# 의존성 설치
npm install

# 개발 서버 실행
npm run dev

# 빌드
npm run build

# 프리뷰
npm run preview
```

### 환경 변수

`.env` 파일 생성:
```env
VITE_API_BASE_URL=http://localhost:8080/api
```

### 접속
- 개발 서버: http://localhost:3000

---

## 📸 주요 화면

### 게시판
- **목록**: 카테고리 필터, 태그 필터, 검색, 썸네일
- **상세**: 이미지 미리보기, 첨부파일, 댓글, 좋아요
- **작성**: Rich Text Editor, 파일/그림 업로드, AI 도우미
- **카테고리 관리**: 생성/수정/삭제, 색상/아이콘 커스터마이징

### 칸반 보드 ⭐
- **보드 목록**: 팀별 보드 리스트
- **칸반 보드**: 3컬럼 레이아웃 (TODO/IN_PROGRESS/DONE)
- **카드 상세**: 우선순위, 담당자, 마감일, 체크리스트
- **드래그 앤 드롭**: 부드러운 애니메이션

---

## 🔐 보안 기능

- **JWT 인증**: LocalStorage 저장
- **토큰 검증**: 만료 시간 체크
- **자동 리다이렉트**: 인증 실패 시 로그인 페이지
- **Protected Routes**: 인증 필요 페이지 보호
- **XSS 방지**: React 자동 이스케이프
- **CORS**: 백엔드와 협력

---

## 🎨 UI/UX 특징

### 반응형 디자인
- 모바일/태블릿/데스크톱 대응
- Tailwind CSS Breakpoints

### 사용자 경험
- **Loading Indicator**: 로딩 상태 표시
- **Error Handling**: 친화적인 에러 메시지
- **Toast 알림**: 성공/실패 피드백
- **Skeleton UI**: 로딩 중 스켈레톤 (선택사항)
- **Optimistic Updates**: 즉각적인 UI 반응 ⭐

### 접근성
- **시맨틱 HTML**: 의미있는 태그
- **키보드 네비게이션**: Tab 지원
- **색상 대비**: WCAG 준수

---

## 📝 개발 가이드

### 커밋 컨벤션
```
feat: 새로운 기능 추가
fix: 버그 수정
docs: 문서 수정
style: 코드 포맷팅
refactor: 코드 리팩토링
test: 테스트 코드
chore: 빌드 업무 수정
```

### 코드 스타일
- **ESLint**: 코드 품질 검사
- **Prettier**: 코드 포맷팅
- **TypeScript**: 타입 안정성

---

## 🐛 트러블슈팅

### 문제: 게시글 작성 시 401 에러
**해결**: 로그인 후 토큰 확인, 백엔드 CORS 설정 확인

### 문제: 칸반 카드 이동 안됨
**해결**: React Query 캐시 무효화 확인

### 문제: 이미지 미리보기 안됨
**해결**: 백엔드 첨부파일 엔드포인트 확인

---

## 📊 성능 최적화

- **React Query 캐싱**: 불필요한 API 호출 감소
- **Lazy Loading**: 페이지 코드 스플리팅
- **Image Optimization**: WebP 지원 (선택)
- **Debounce**: 검색 입력 최적화
- **Memoization**: React.memo, useMemo 활용

---

## 🔮 향후 계획

- [ ] 실시간 알림 (WebSocket)
- [ ] 다크 모드
- [ ] PWA 지원
- [ ] 무한 스크롤
- [ ] 이미지 편집기
- [ ] 마크다운 지원
- [ ] 칸반 보드 커스터마이징

---

## 👨‍💻 개발자
- **이름**: 성진 (Lee)
- **기간**: 2026.01
- **역할**: Full-Stack Developer

---

## 📄 라이선스
MIT License
