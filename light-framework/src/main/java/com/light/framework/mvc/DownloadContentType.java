package com.light.framework.mvc;

public enum DownloadContentType {
    DEFAULT("application/octet-stream;charset=utf-8"),

    doc("application/msword"), docx("application/msword"),

    xls("application/vnd.ms-excel"), xlsx("application/x-xls"),

    ppt("application/vnd.ms-powerpoint"), pptx("application/x-ppt"),

    vsd("application/vnd.visio"), vxdx("application/x-vsd"),

    pdf("application/pdf"),

    dll("application/x-msdownload"),

    exe("application/x-msdownload"), pfx("application/x-pkcs12"),

    fax("image/fax"), img("application/x-img"), jfif("image/jpeg"), jpe("image/jpeg"), xjpe("application/x-jpe"),
    jpeg("image/jpeg"), jpg("image/jpeg"), png("image/png"), xpng("application/x-png"), tif("image/tiff"),
    xtif("application/x-tif"), tiff("image/tiff"), xjpg("application/x-jpg"),

    m3u("audio/mpegurl"), mid("audio/mid"), midi("audio/mid"), mnd("audio/x-musicnet-download"),
    mns("audio/x-musicnet-stream"), movie("video/x-sgi-movie"), mp1("audio/mp1"), mp2("audio/mp2"), mp3("audio/mp3"),
    mpga("audio/rn-mpeg"), wav("audio/wav"), wma("audio/x-ms-wma"),

    m1v("video/x-mpeg"), m2v("video/x-mpeg"), m4e("video/mpeg4"), mp2v("video/mpeg"), mp4("video/mpeg4"),
    mpa("video/x-mpg"), mpe("video/x-mpeg"), mpeg("video/mpg"), mps("video/x-mpeg"), mpv("video/mpg"),
    mpv2("video/mpeg"), mpg("video/mpg"), avi("video/avi"), IVF("video/x-ivf");

    private String value;

    DownloadContentType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
