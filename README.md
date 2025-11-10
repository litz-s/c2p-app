# C2P - Copy/Paste Tables (JavaFX)

小さな実用アプリ。複数の「コピペテーブル」を管理し、各テーブル内で大量のテキストスニペットをスクロールしながら扱えます。
- メイン画面右上の「＋」で新規テーブルを作成（名前 + Auto C to P 初期設定）
- 「編集」で既存テーブルの名前変更、Auto C to P 切替、削除
- テーブル画面では：
  - 行（テキストエリア）は**シングルクリックでコピー**、**ダブルクリックで編集**、**フォーカスアウトで保存**
  - 編集中は Enter で改行、Tab でインデント
  - 「Auto C to P」有効時は**クリップボード内容の変化を自動取得**して行を追加（ポーリング方式）

## 必要環境
- OpenJDK 21（推奨）
- Gradle 8系（VSCodeを使う場合はJava Extension Packが入っていればビルド/実行しやすいです）

## 実行方法（Windows / macOS / Linux）
```bash
# 1) プロジェクト直下で（gradleラッパーが未生成の場合）
gradle wrapper

# 2) 実行
./gradlew run      # macOS/Linux
.\gradlew run      # Windows PowerShell
```

VSCode なら、拡張機能「Java Extension Pack」を入れた上で、`App.java` を開いて「Run」ボタンから起動できます。

## 保存先
ユーザーホーム直下の `~/.c2p/c2p.json` にテーブルデータを保存します。

## 注意
- Auto C to P は OS 全体の Ctrl+C を直接フックしているわけではありません。JavaFX からはシステムクリップボードの**内容変化**を 300ms 間隔で監視し、変化があれば新規行として追加します。（他アプリ由来のコピーも取得されます）
- 大量の行も `ListView` の仮想化で軽快に動作しますが、極端に大きいテキストを大量に詰めるとパフォーマンスが落ちる場合があります。
