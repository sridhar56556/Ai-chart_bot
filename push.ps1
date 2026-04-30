# Auto-push changes to GitHub
git add .
$timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
git commit -m "Auto-update: $timestamp"
git push origin main
Write-Host "✅ Changes pushed to GitHub successfully!" -ForegroundColor Green
