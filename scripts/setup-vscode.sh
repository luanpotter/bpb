#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

KOTLIN_LSP_VERSION="262.2310.0"
VSIX_URL="https://download-cdn.jetbrains.com/kotlin-lsp/${KOTLIN_LSP_VERSION}/kotlin-lsp-${KOTLIN_LSP_VERSION}-linux-x64.vsix"

remove_old_kotlin_extension() {
  if code --list-extensions | grep -q "fwcd.kotlin"; then
    echo "Removing old fwcd.kotlin extension..."
    code --uninstall-extension fwcd.kotlin
  fi
}

install_kotlin_lsp() {
  if code --list-extensions | grep -q "jetbrains.kotlin"; then
    echo "JetBrains Kotlin LSP is already installed."
    return
  fi

  echo "Downloading JetBrains Kotlin LSP v${KOTLIN_LSP_VERSION}..."
  local vsix_path
  vsix_path=$(mktemp --suffix=.vsix)
  curl -fSL -o "$vsix_path" "$VSIX_URL"

  echo "Installing Kotlin LSP extension..."
  code --install-extension "$vsix_path"
  rm -f "$vsix_path"
}

patch_kotlin_lsp() {
  local ext_dir
  ext_dir="$(find "$HOME/.vscode/extensions" -maxdepth 1 -name 'jetbrains.kotlin-*' -type d | head -1)"
  if [[ -z "$ext_dir" ]]; then
    echo "JetBrains Kotlin extension not found, skipping patch."
    return
  fi

  local ext_js="${ext_dir}/dist/extension.js"
  if grep -q 'willSaveWaitUntil&&!1&&this.register' "$ext_js" 2>/dev/null; then
    echo "Kotlin LSP already patched (willSaveWaitUntil disabled)."
    return
  fi

  if grep -q 'willSaveWaitUntil&&this.register' "$ext_js" 2>/dev/null; then
    echo "Patching Kotlin LSP to disable willSaveWaitUntil formatting..."
    sed -i 's/willSaveWaitUntil&&this\.register/willSaveWaitUntil\&\&!1\&\&this.register/g' "$ext_js"
    echo "Patched successfully."
  else
    echo "WARNING: Could not find willSaveWaitUntil pattern — extension may have changed."
  fi
}

install_runonsave() {
  if code --list-extensions | grep -q "emeraldwalk.RunOnSave"; then
    echo "RunOnSave is already installed."
    return
  fi

  echo "Installing RunOnSave extension..."
  code --install-extension emeraldwalk.RunOnSave
}

configure_settings() {
  mkdir -p .vscode
  cat > .vscode/settings.json << 'EOF'
{
  "[kotlin]": {
    "editor.formatOnSave": false,
    "editor.codeActionsOnSave": {}
  },
  "emeraldwalk.runonsave": {
    "commands": [
      {
        "match": "\\.kt$",
        "cmd": "${workspaceFolder}/scripts/ktfmt.sh ${file}"
      }
    ]
  }
}
EOF
  echo "Configured .vscode/settings.json."
}

download_ktfmt() {
  echo "Pre-downloading ktfmt JAR..."
  ./scripts/ktfmt.sh --check src/main/kotlin/xyz/luan/bpb/Main.kt > /dev/null 2>&1 || true
  echo "ktfmt JAR cached."
}

main() {
  echo "Setting up VS Code for Kotlin development..."
  remove_old_kotlin_extension
  install_kotlin_lsp
  patch_kotlin_lsp
  install_runonsave
  configure_settings
  download_ktfmt
  echo "Done! Reload VS Code to activate."
}

main
