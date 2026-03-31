#!/usr/bin/env bash
set -euo pipefail

KOTLIN_LSP_VERSION="262.2310.0"
VSIX_URL="https://download-cdn.jetbrains.com/kotlin-lsp/${KOTLIN_LSP_VERSION}/kotlin-lsp-${KOTLIN_LSP_VERSION}-linux-x64.vsix"

echo "Setting up VS Code for Kotlin development..."

# Remove old fwcd.kotlin extension if present
if code --list-extensions | grep -q "fwcd.kotlin"; then
  echo "Removing old fwcd.kotlin extension..."
  code --uninstall-extension fwcd.kotlin
fi

# Check if JetBrains kotlin-lsp is already installed
if code --list-extensions | grep -q "jetbrains.kotlin"; then
  echo "JetBrains Kotlin LSP is already installed."
  echo "To upgrade, uninstall it first: code --uninstall-extension jetbrains.kotlin"
  exit 0
fi

echo "Downloading JetBrains Kotlin LSP v${KOTLIN_LSP_VERSION}..."
VSIX_PATH=$(mktemp --suffix=.vsix)
curl -fSL -o "$VSIX_PATH" "$VSIX_URL"

echo "Installing extension..."
code --install-extension "$VSIX_PATH"
rm -f "$VSIX_PATH"

echo "Done! Reload VS Code to activate the extension."
