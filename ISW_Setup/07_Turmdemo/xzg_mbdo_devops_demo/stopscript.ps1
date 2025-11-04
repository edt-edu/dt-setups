Get-Process -Name Code | Where-Object { $_.Path -like "*Microsoft VS Code*" } | Stop-Process -Force
