# Key Quota 2.1.9 · 混沌键盘

This is an Android input method and a small native chat for testing it. Install the APK, open Key Quota, tap **Enable keyboard**, then **Choose input method**. Select a mode and open **Try the demo chat**, or focus a text field in another app. The top strip switches modes between drafts or opens the system keyboard picker.

## Delivery

Tap the keyboard draft to position the caret. Typing inserts at that position. In the seven collision modes, three identical characters immediately before the caret unlock deletion of the whole word around that triple. Each theme keeps its refill and dial rules. Confirmation inserts the draft once at the active Android editor selection. The user presses the host app’s own Send button. Only balances and unfinished refills are persisted.

Chalk keeps swipe suggestions, word undo, local trends, sponsor breaks and its own dial choices. Phantom keeps disappearing keys, whole-draft deletion, speed controls, individual reload odds and its original win/loss outcomes. Its video slots use the three packaged clips in place of the original desktop-only paths.

## Add a version

Prism Pulse is the ninth option. Its eight key colours, contrasting borders, grid field and function keys follow the Rainbow theme in the supplied APK. Available keys flash across the entire keycap on staggered cycles. Depleted keys and the reduced-motion setting disable the animated glow.

1. Put a self-contained keyboard page in `app/src/main/assets/keyboards/`. Shared clips can be referenced under `media/`.
2. Add an entry to `versions.json` with a unique lowercase `id`, the local `page`, a bilingual `name`, `tag` and `intro`, and a home-card `palette` such as `industrial` or `pixel`. The home screen and IME mode switch both use this registry.
3. Include `host.js` before the keyboard script and implement `window.kqIsIdle()` so the mode switch can protect unfinished work. Call `KQHost.deliver(exactDraft)` only at the version's delivery point. Consume the draft only when the call returns true.
4. Modes using the shared 36-key wallet call `KQHost.init(remaining)` once after key initialization and restore its optional reload ticket. Pass balances and the current ticket together to `KQHost.save(remaining,ticketOrNull)`. The helper stores changes relative to the loaded balances. An advertising exit should bank winnings and clear its ticket in the same update.

## Build

Open this folder as a Gradle project in Android Studio, with JDK 17 and Android SDK 35. The application module also supports `gradle :app:assembleDebug` with a compatible installed Gradle. On Windows, `build.ps1 -Sdk <sdk-directory> -Jdk <jdk-directory> -Keystore <your-keystore>` builds and signs without Gradle. A signing key is not included. The script also recognizes the original project's `tools/sdk` and `tools/jdk17` layouts. Set the environment variable `KQ_KEYSTORE_PASSWORD` for a custom signing password. A local test key is generated if a signing key is absent.
