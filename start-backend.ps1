# Load .env file and set environment variables for this process
Get-Content .env | ForEach-Object {
    if ($_ -match '^\s*([^#][^=]+)=(.*)$') {
        $name = $matches[1].Trim()
        $value = $matches[2].Trim()
        [Environment]::SetEnvironmentVariable($name, $value)
        Write-Host "Set $name"
    }
}

Write-Host ""
Write-Host "Starting Spring Boot backend on port 8081..." -ForegroundColor Green
Write-Host ""

# Start Maven Spring Boot with environment variables
$env:DB_HOST="localhost"
$env:DB_PORT="3306"
$env:DB_NAME="auth_app_db"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="Vikas@123"
$env:JWT_SECRET="KVmP5KfaJ9iRDNKCiyTuulnYp+W8u06pxvBeSTTtAkImJ5MKwdlUunXjrGkaCR1t"
$env:JWT_EXPIRATION="3600000"
$env:CORS_ALLOWED_ORIGINS="http://localhost:5173"

mvn spring-boot:run
