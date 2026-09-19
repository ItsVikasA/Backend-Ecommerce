# Fix MySQL Service - Point to MySQL 8.4
# Run this as Administrator

Write-Host "Stopping MySQL service if running..." -ForegroundColor Yellow
Stop-Service MySQL -ErrorAction SilentlyContinue

Write-Host "Removing old MySQL service..." -ForegroundColor Yellow
sc.exe delete MySQL

Write-Host "Installing MySQL 8.4 service..." -ForegroundColor Green
& "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysqld.exe" --install MySQL --defaults-file="C:\Program Files\MySQL\MySQL Server 8.4\my.ini"

Write-Host "Starting MySQL service..." -ForegroundColor Green
Start-Service MySQL

Write-Host ""
Write-Host "MySQL service has been updated and started!" -ForegroundColor Green
Write-Host "You can now connect from MySQL Workbench." -ForegroundColor Cyan
