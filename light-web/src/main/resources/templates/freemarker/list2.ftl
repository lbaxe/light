<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Title freemarker</title>
</head>
<body>
<#list list as menu>
    <tr>
        <td>${menu.menuId}</td>
        <td>${menu.menuName}</td>
    </tr>
</#list>
</body>
</html>