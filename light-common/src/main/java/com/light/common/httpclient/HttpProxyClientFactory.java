package com.light.common.httpclient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.nio.conn.NoopIOSessionStrategy;
import org.apache.http.nio.conn.SchemeIOSessionStrategy;
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.Args;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.light.common.httpclient.asyn.AbstractFutureCallback;
import com.light.common.httpclient.asyn.HttpAsynProxyClient;

public class HttpProxyClientFactory {
    private static Logger logger = LoggerFactory.getLogger(HttpProxyClientFactory.class);

    public static final int MAX_TOTAL_CONNECTIONS_DEFAULT = 512;

    public static final int MAX_PER_ROUTE_DEFAULT = 128;

    public static final int SOCKET_BUFFER_SIZE_DEFAULT = 8192;

    public static final int SOCKET_TIMEOUT_DEFAULT = 10000;

    public static final int CONNECTION_TIMEOUT_DEFAULT = 10000;

    public static final int CONNECTION_REQUEST_TIMEOUT_DEFAULT = 10000;

    public static final int RETRY_COUNT_DEFAULT = 3;

    private int maxTotalConnections = MAX_TOTAL_CONNECTIONS_DEFAULT;

    private int defaultMaxPerRoute = MAX_PER_ROUTE_DEFAULT;

    private int socketBufferSize = SOCKET_BUFFER_SIZE_DEFAULT;
    /**
     * 建立tcp连接超时时间
     */
    private int connectionTimeOut = CONNECTION_TIMEOUT_DEFAULT;
    /**
     * 连接建立后，数据传输超时时间
     */
    private int socketTimeOut = SOCKET_TIMEOUT_DEFAULT;
    /**
     * 数据传输过程中数据包之间间隔的最大时间
     */
    private int connectionRequestTimeOut = CONNECTION_REQUEST_TIMEOUT_DEFAULT;
    /**
     * 重试次数
     */
    private int retryCount = 3;

    private static volatile HttpProxyClient instance = null;
    private static volatile HttpAsynProxyClient aysnInstance = null;

    private static byte[] lock1 = new byte[0];
    private static byte[] lock2 = new byte[0];

    public static HttpProxyClient getInstance() {
        if (instance == null) {
            synchronized (lock1) {
                if (instance == null) {
                    instance = httpSendClient(3000, 2000, 2000);
                    logger.info("HttpSendClientFactory New HttpSendClient Instance");
                }
            }
        }
        return instance;
    }

    public static HttpAsynProxyClient getAsynInstance() {
        if (aysnInstance == null) {
            synchronized (lock2) {
                if (aysnInstance == null) {
                    aysnInstance = httpAsynSendClient(3000, 2000, 2000);
                    logger.info("HttpSendClientFactory New HttpAsynSendClient Instance");
                }
            }
        }
        return aysnInstance;
    }

    private static HttpProxyClient httpSendClient(int socketTimeOut, int connectionTimeOut,
        int connectionRequestTimeOut) {
        HttpProxyClientFactory factory = new HttpProxyClientFactory();
        factory.setSocketTimeOut(socketTimeOut);
        factory.setConnectionTimeOut(connectionTimeOut);
        factory.setConnectionRequestTimeOut(connectionRequestTimeOut);
        return factory.newHttpSendClient();
    }

    private static HttpAsynProxyClient httpAsynSendClient(int socketTimeOut, int connectionTimeOut,
        int connectionRequestTimeOut) {
        HttpProxyClientFactory factory = new HttpProxyClientFactory();
        factory.setSocketTimeOut(socketTimeOut);
        factory.setConnectionTimeOut(connectionTimeOut);
        factory.setConnectionRequestTimeOut(connectionRequestTimeOut);
        return factory.newHttpAsynSendClient();
    }

    public HttpProxyClient newHttpSendClient() {
        return new HttpProxyClient(buildCloseableHttpClient());
    }

    public HttpAsynProxyClient newHttpAsynSendClient() {
        return new HttpAsynProxyClient(buildCloseableHttpAsyncClient());
    }

    private CloseableHttpAsyncClient buildCloseableHttpAsyncClient() {
        // 配置io线程
        IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
            .setIoThreadCount(Runtime.getRuntime().availableProcessors()).setSoKeepAlive(true).build();
        // 设置连接池大小
        ConnectingIOReactor ioReactor = null;
        try {
            ioReactor = new DefaultConnectingIOReactor(ioReactorConfig);
        } catch (IOReactorException e) {
            logger.error("初始化异步连接池配置异常", e);
        }

        Registry<SchemeIOSessionStrategy> ioSessionFactoryRegistry =
            RegistryBuilder.<SchemeIOSessionStrategy>create().register("http", NoopIOSessionStrategy.INSTANCE)
                // .register("https", SSLIOSessionStrategy.getDefaultStrategy()).build();
                .register("https", new SSLIOSessionStrategy(createSSLContext(), (s, sslSession) -> true)).build();

        PoolingNHttpClientConnectionManager connectionManager =
            new PoolingNHttpClientConnectionManager(ioReactor, ioSessionFactoryRegistry);
        connectionManager.setDefaultMaxPerRoute(this.defaultMaxPerRoute);
        connectionManager.setMaxTotal(this.maxTotalConnections);

        ConnectionConfig connectionConfig = ConnectionConfig.custom().setCharset(StandardCharsets.UTF_8).build();
        connectionManager.setDefaultConnectionConfig(connectionConfig);

        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(this.socketTimeOut)
            .setConnectTimeout(this.connectionTimeOut).setConnectionRequestTimeout(this.connectionRequestTimeOut)// 从连接池获取连接超时时间
            .build();
        return HttpAsyncClients.custom().setConnectionManager(connectionManager).setDefaultRequestConfig(requestConfig)
            .setKeepAliveStrategy(this.getConnectionKeepAliveStrategy()).build();
    }

    private CloseableHttpClient buildCloseableHttpClient() {
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
            .register("http", PlainConnectionSocketFactory.getSocketFactory())
            // .register("https", SSLConnectionSocketFactory.getSocketFactory())
            .register("https", new SSLConnectionSocketFactory(this.createSSLContext(), (s, sslSession) -> true))
            .build();

        PoolingHttpClientConnectionManager connectionManager =
            new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        // 连接池最大连接数
        connectionManager.setMaxTotal(this.maxTotalConnections);
        // 每个路由或主机最大连接数
        connectionManager.setDefaultMaxPerRoute(this.defaultMaxPerRoute);

        ConnectionConfig connectionConfig = ConnectionConfig.custom().setCharset(StandardCharsets.UTF_8).build();
        connectionManager.setDefaultConnectionConfig(connectionConfig);

        SocketConfig socketConfig = SocketConfig.custom().setTcpNoDelay(true)// 是否立即发送数据，true关闭socket缓冲
            .setSoTimeout(this.socketTimeOut).setSndBufSize(this.socketBufferSize)// 发送缓冲区
            .setRcvBufSize(this.socketBufferSize)// 接收缓冲区
            .setSoKeepAlive(true)// 开启监视TCP连接是否有效
            .build();
        connectionManager.setDefaultSocketConfig(socketConfig);

        RequestConfig requestConfig =
            RequestConfig.custom().setSocketTimeout(this.socketTimeOut).setConnectTimeout(this.connectionTimeOut)
                .setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT_DEFAULT).build();

        // 重试策略
        DefaultHttpRequestRetryHandler retryHandler = new DefaultHttpRequestRetryHandler(this.retryCount, false);
        return HttpClients.custom().setConnectionManager(connectionManager).setDefaultRequestConfig(requestConfig)
            .setRetryHandler(retryHandler).setKeepAliveStrategy(this.getConnectionKeepAliveStrategy()).build();
    }

    private ConnectionKeepAliveStrategy getConnectionKeepAliveStrategy() {
        return (response, context) -> {
            Args.notNull(response, "HTTP response");
            final HeaderElementIterator it =
                new BasicHeaderElementIterator(response.headerIterator(HTTP.CONN_KEEP_ALIVE));
            while (it.hasNext()) {
                final HeaderElement he = it.nextElement();
                final String param = he.getName();
                final String value = he.getValue();
                if (value != null && param.equalsIgnoreCase("timeout")) {
                    try {
                        return Long.parseLong(value) * 1000;
                    } catch (final NumberFormatException ignore) {
                    }
                }
            }
            /*
             * HttpHost target = (HttpHost) context.getAttribute(
             * HttpClientContext.HTTP_TARGET_HOST); if
             * ("https://www.baidu.com/".equalsIgnoreCase(target.getHostName())) { // Keep
             * alive for 5 seconds only return 5 * 1000; } else { // otherwise keep alive
             * for 30 seconds return 30 * 1000; }
             */
            return 10 * 1000;
        };
    }

    private SSLContext createSSLContext() {
        SSLContext ctx = null;
        try {
            // SSLContext ctx = SSLContexts.custom().setProtocol("SSL").build();
            ctx = SSLContext.getInstance("SSL");
            ctx.init(null, new TrustManager[] {new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            }}, null);
        } catch (NoSuchAlgorithmException e) {
            logger.error("SSLContext实例化异常", e);
        } catch (KeyManagementException e) {
            logger.error("SSLContext初始化异常", e);
        }

        return ctx;
    }

    public int getMaxTotalConnections() {
        return maxTotalConnections;
    }

    public void setMaxTotalConnections(int maxTotalConnections) {
        this.maxTotalConnections = maxTotalConnections;
    }

    public int getDefaultMaxPerRoute() {
        return defaultMaxPerRoute;
    }

    public void setDefaultMaxPerRoute(int defaultMaxPerRoute) {
        this.defaultMaxPerRoute = defaultMaxPerRoute;
    }

    public int getSocketBufferSize() {
        return socketBufferSize;
    }

    public void setSocketBufferSize(int socketBufferSize) {
        this.socketBufferSize = socketBufferSize;
    }

    public int getConnectionTimeOut() {
        return connectionTimeOut;
    }

    public void setConnectionTimeOut(int connectionTimeOut) {
        this.connectionTimeOut = connectionTimeOut;
    }

    public int getSocketTimeOut() {
        return socketTimeOut;
    }

    public void setSocketTimeOut(int socketTimeOut) {
        this.socketTimeOut = socketTimeOut;
    }

    public int getConnectionRequestTimeOut() {
        return connectionRequestTimeOut;
    }

    public void setConnectionRequestTimeOut(int connectionRequestTimeOut) {
        this.connectionRequestTimeOut = connectionRequestTimeOut;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException, IOException {
        HttpProxyClient httpSendClient = HttpProxyClientFactory.getInstance();
        HttpProxyClientFactory.getAsynInstance().get("https://www.baidu.com", null, null, new AbstractFutureCallback() {
            @Override
            protected void doCompleted(HttpResponse httpResponse) {
                try {
                    System.out.println(EntityUtils.toString(httpResponse.getEntity()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
