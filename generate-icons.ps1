Add-Type -AssemblyName System.Drawing

function CreateIcon($size, $path) {
    $bmp = New-Object System.Drawing.Bitmap $size, $size
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    
    # 1. Background fill
    $g.Clear([System.Drawing.Color]::FromArgb(0xFF, 0x08, 0x50, 0x41))
    
    # 2. Draw leaf shape (curves)
    $brush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(0xFF, 0x1D, 0x9E, 0x75))
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    
    # Simple leaf path
    $pathObj = New-Object System.Drawing.Drawing2D.GraphicsPath
    
    $p1 = New-Object System.Drawing.PointF ($size * 0.5), ($size * 0.15)
    $p2 = New-Object System.Drawing.PointF ($size * 0.8), ($size * 0.5)
    $p3 = New-Object System.Drawing.PointF ($size * 0.5), ($size * 0.85)
    $p4 = New-Object System.Drawing.PointF ($size * 0.2), ($size * 0.5)
    
    $pathObj.AddBezier($p1, $p2, $p2, $p3)
    $pathObj.AddBezier($p3, $p4, $p4, $p1)
    
    $g.FillPath($brush, $pathObj)
    
    # 3. Draw veins
    $veinPen = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(0xFF, 0x08, 0x50, 0x41), ($size * 0.025))
    $g.DrawLine($veinPen, ($size * 0.5), ($size * 0.15), ($size * 0.5), ($size * 0.85))
    
    $g.DrawLine($veinPen, ($size * 0.5), ($size * 0.4), ($size * 0.65), ($size * 0.3))
    $g.DrawLine($veinPen, ($size * 0.5), ($size * 0.55), ($size * 0.35), ($size * 0.45))
    $g.DrawLine($veinPen, ($size * 0.5), ($size * 0.7), ($size * 0.65), ($size * 0.6))
    
    # Cleanup
    $veinPen.Dispose()
    $pathObj.Dispose()
    $brush.Dispose()
    $g.Dispose()
    
    $bmp.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Dispose()
}

$dir = "c:\Users\D E L L\Desktop\Food Bridge\foodbridge-frontend\public\icons"
if (-not (Test-Path $dir)) { New-Item -ItemType Directory -Path $dir -Force }

CreateIcon 192 "$dir\icon-192.png"
CreateIcon 512 "$dir\icon-512.png"

# Also output simple SVG file
$svgPath = "c:\Users\D E L L\Desktop\Food Bridge\foodbridge-frontend\public\icons\icon.svg"
$svgContent = @"
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100" width="100" height="100">
  <rect width="100" height="100" rx="20" fill="#085041"/>
  <path d="M50,15 C65,15 80,35 80,50 C80,65 65,85 50,85 C35,85 20,65 20,50 C20,35 35,15 50,15 Z" fill="#1D9E75"/>
  <path d="M50,15 C50,15 50,45 50,85" stroke="#085041" stroke-width="3" fill="none"/>
  <path d="M50,40 C55,42 65,40 70,35" stroke="#085041" stroke-width="2.5" fill="none"/>
  <path d="M50,55 C45,57 35,55 30,50" stroke="#085041" stroke-width="2.5" fill="none"/>
  <path d="M50,70 C55,72 65,70 70,65" stroke="#085041" stroke-width="2.5" fill="none"/>
</svg>
"@

[System.IO.File]::WriteAllText($svgPath, $svgContent)
Write-Output "Icons generated successfully!"
