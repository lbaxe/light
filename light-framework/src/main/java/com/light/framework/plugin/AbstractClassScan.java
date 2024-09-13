package com.light.framework.plugin;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.SystemPropertyUtils;

import com.light.core.util.SpringContextUtil;

public abstract class AbstractClassScan {
    private static final Logger logger = LoggerFactory.getLogger(AbstractClassScan.class);

    private PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    private MetadataReaderFactory readerFactory = new SimpleMetadataReaderFactory();

    private static final String JAR_FILE_CLASS = "!/BOOT-INF/classes!/";

    protected abstract String path();

    public abstract boolean conventional(Class<?> clazz);

    protected boolean isContainJar() {
        return true;
    }

    public List<Class<?>> scan() {
        List<Class<?>> list = new ArrayList<>();
        String[] basePackages =
            StringUtils.tokenizeToStringArray(this.path(), ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS);
        for (String basePackage : basePackages) {
            String resourcePath =
                ClassUtils.convertClassNameToResourcePath(SystemPropertyUtils.resolvePlaceholders(basePackage))
                    + ".class";
            try {
                Resource[] resources =
                    resolver.getResources(ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + resourcePath);
                for (Resource resource : resources) {
                    if (resource.isReadable()) {
                        // 是否扫描jar扩展包（排除SpringBoot 运行jar）
                        if (!isContainJar() && ResourceUtils.isJarURL(resource.getURL())
                            && !isClassURL(resource.getURL())) {
                            continue;
                        }
                        MetadataReader reader = this.readerFactory.getMetadataReader(resource);
                        String className = reader.getClassMetadata().getClassName();
                        Class<?> clazz = Class.forName(className);
                        if (conventional(clazz)) {
                            list.add(clazz);
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("AbstractClassScan scan fail", e);
            }
        }
        return list;
    }
    protected boolean isClassURL(URL url) {
        return url.getPath().contains(JAR_FILE_CLASS);
    }

    protected void checkBean(Class<?> clazz) {
        SpringContextUtil.getBean(clazz);
    }

    protected void checkBean(String beanName) {
        SpringContextUtil.getBean(beanName);
    }
}
