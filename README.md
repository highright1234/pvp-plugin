# PvpPlugin
대충 쓸려고 만든 pvp 플러그인

## How to use
`/pvp kd`   
kd 확인   
`/pvp admin reload`   
config 리로드

## //TODO
- kd 확인 명령어 아직 안만들었음
- 킷 설정

## 기능
싸우는중에는 다른 플레이어 못떄림

### config 값들
- pvp-continue-time: 누가 때리고 설정한 이 값만큼 시간이 지나면 싸우는 상태 제거(ms 단위)
- is-debug: true: 디버그 메시지
- start-action: 싸우려 할때 액션 ( ALL / HAND / BOW / CLOSE_WEAPON )
- hiding-players-when-fighting: 싸우는중 다른플레이어 숨기기
- activation-world: 이 기능을 적용할 월드
- hiding-fighting-players: 싸우고 있는 플레이어들을 숨기기

## TMI

사실 코드 짜다가 알게된거였는데 플레이어 싸우는중에 투명으로 보이던거나   
다른 그런기능이 이미 어느 서버에 있어서 클론코딩 비스무리하게 되버린


## Used Libraries
> Coroutine   
> [MCCoroutine](https://github.com/Shynixn/MCCoroutine)   
> [Kommand](https://github.com/monun/kommand)   
> [InvFX](https://github.com/monun/invfx)   
> [Tap](https://github.com/monun/tap)   
