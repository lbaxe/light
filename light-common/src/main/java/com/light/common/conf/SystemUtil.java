package com.light.common.conf;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.springframework.boot.system.ApplicationHome;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

public final class SystemUtil {
    public static final String FILE_SEPARATOR = System.getProperty("file.separator");

    private static String deployDir;
    static {
        deployDir = getDeployDir();
    }

    private static String getDeployDir() {
        ApplicationHome appHome1 = new ApplicationHome();
        System.getProperty("user.dir");
        // 有启动配置
        if (appHome1.getSource() != null) {
            return appHome1.getDir().getPath();
        } else {// 无启动配置
            ApplicationHome appHome2 = new ApplicationHome(SystemUtil.class);
            String path = appHome2.getDir().getPath();
            int separator = path.indexOf("target" + FILE_SEPARATOR);
            if (separator > 0) {
                return System.getProperty("user.dir");
            }
            return path;
        }
    }

    private static File findSource(URL location) throws IOException, URISyntaxException {
        URLConnection connection = location.openConnection();
        if (connection instanceof JarURLConnection) {
            return getRootJarFile(((JarURLConnection)connection).getJarFile());
        }
        return new File(location.toURI());
    }

    private static File getRootJarFile(JarFile jarFile) {
        String name = jarFile.getName();
        int separator = name.indexOf("!/");
        if (separator > 0) {
            name = name.substring(0, separator);
        }
        return new File(name);
    }

    private static File findHomeDir(File source) {
        File homeDir = source;
        homeDir = (homeDir != null) ? homeDir : findDefaultHomeDir();
        if (homeDir.isFile()) {
            homeDir = homeDir.getParentFile();
        }
        homeDir = homeDir.exists() ? homeDir : new File(".");
        return homeDir.getAbsoluteFile();
    }

    private static File findDefaultHomeDir() {
        String userDir = System.getProperty("user.dir");
        return new File(StringUtils.hasLength(userDir) ? userDir : ".");
    }

    private static Class<?> getStartClass() {
        try {
            ClassLoader classLoader = SystemUtil.class.getClassLoader();
            return getStartClass(classLoader.getResources("META-INF/MANIFEST.MF"));
        } catch (Exception ex) {
            return null;
        }
    }

    private static Class<?> getStartClass(Enumeration<URL> manifestResources) {
        while (manifestResources.hasMoreElements()) {
            try (InputStream inputStream = manifestResources.nextElement().openStream()) {
                Manifest manifest = new Manifest(inputStream);
                String startClass = manifest.getMainAttributes().getValue("Start-Class");
                if (startClass != null) {
                    return ClassUtils.forName(startClass, SystemUtil.class.getClassLoader());
                }
            } catch (Exception ex) {
            }
        }
        return null;
    }

    public static String getDeployDirectory(String subDirectory) {
        return deployDir + FILE_SEPARATOR + ((subDirectory != null) ? subDirectory + FILE_SEPARATOR : "");
    }

    public static String getDeployDirectory() {
        return deployDir;
    }

    public static void main(String[] args) {
        ApplicationHome home = new ApplicationHome(SystemUtil.class); // 使用当前类的Class对象
        File dir = home.getDir(); // 获取应用程序所在的目录
        System.out.println(SystemUtil.getDeployDirectory());
        // File jar = home.getJar(); // 获取应用程序所在的Jar包（如果有的话）
        File source = home.getSource(); // 获取应用程序源码路径（如果可用）
    }
}
