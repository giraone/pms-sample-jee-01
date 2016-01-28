package com.giraone.samples.pmspoc1.boundary.blobs;

import java.util.HashMap;

//--------------------------------------------------------------------------------

/**
 * Utilities to handle different image and multimedia formats.
 */
public class MimeTypeUtil
{
    /**
     * Return MIME type for a file name or path
     * @param fileName file name
     * @return A MIME type (DEFAULT is "application/octet-stream";)
     */
    public static String getMimeTypeForFile(String fileName)
    {
        final int lastDot = fileName.lastIndexOf('.');
        if (lastDot == -1)
            return MIME_DEFAULT;
        else if (lastDot == (fileName.length()-1))
            return MIME_DEFAULT;
        else
            return getMimeType(fileName.substring(lastDot+1));
    }

    /**
     * Return MIME type for a given extension.
     * @param extension extension (without leading dot)
     * @return A MIME type (DEFAULT is "application/octet-stream";)
     */
    public static String getMimeType(String extension)
    {
        if (extension == null)
            return MIME_DEFAULT;
        String ret = (String) MAP_EXT2MIME.get(extension.toLowerCase());
        if (ret != null)
            return ret;
        else
            return MIME_DEFAULT;
    }

    /**
     * Return filename extension for a given MIME type.
     * @param mimeType the MIME type
     * @return A MIME type (DEFAULT is "img";)
     */
    public static String getExtension(String mimeType)
    {
        if (mimeType == null)
            return EXT_DEFAULT;
        String ret = (String) MAP_MIME2EXT.get(mimeType.toLowerCase());
        if (ret != null)
            return ret;
        else
            return EXT_DEFAULT;
    }

    /**
     * Return true, if the given MIME type is a known type.
     * @param mimeType
     */
    public static boolean isKnownType(String mimeType)
    {
    	if (mimeType == null)
    		return false;
    	return MAP_MIME2EXT.get(mimeType.toLowerCase()) != null;
    }
    
    /**
     * Return true, if the given MIME type is an image.
     * @param mimeType
     */
    public static boolean isImageBitmap(String mimeType)
    {
        return mimeType != null && mimeType.startsWith("image");
    }

    /**
     * Return true, if the given MIME type is a video.
     * @param mimeType
     */
    public static boolean isVideo(String mimeType)
    {
        return mimeType != null && mimeType.startsWith("video");
    }
    
    //--------------------------------------------------------------------------------

    /**
     * The default MIME type (application/octet-stream) as a string constant
     */
    public static final String MIME_DEFAULT = "application/octet-stream";
    /**
     * The default MIME type's extension
     */
    public static final String EXT_DEFAULT = "bin";
    /**
     * Useful MIME type (text/html) as a string constant
     */
    public static final String MIME_HTML = "text/html";
    /**
     * Useful MIME type (application/pdf) as a string constant
     */
    public static final String MIME_PDF = "application/pdf";
    /**
     * Useful MIME type (image/jpeg) as a string constant
     */
    public static final String MIME_JPEG = "image/jpeg";
    /**
     * Useful MIME type (image/png) as a string constant
     */
    public static final String MIME_PNG = "image/png";

    public static final HashMap<String,String> MAP_EXT2MIME = new HashMap<String,String>();
    public static final HashMap<String,String> MAP_MIME2EXT = new HashMap<String,String>();

    static
    {
        MAP_EXT2MIME.put("jpeg", MIME_JPEG);
        MAP_EXT2MIME.put("jpg",  MIME_JPEG);
        MAP_EXT2MIME.put("jp2", "image/jp2");
        MAP_EXT2MIME.put("tif", "image/tiff");
        MAP_EXT2MIME.put("tiff", "image/tiff");
        MAP_EXT2MIME.put("png", MIME_PNG);
        MAP_EXT2MIME.put("bmp", "image/bmp");
        MAP_EXT2MIME.put("gif", "image/gif");

        MAP_EXT2MIME.put("pdf", MIME_PDF);
        MAP_EXT2MIME.put("fdf", "application/vnd.fdf");
        MAP_EXT2MIME.put("xfdf", "application/vnd.adobe.xfdf");
        MAP_EXT2MIME.put("doc", "application/msword");
        MAP_EXT2MIME.put("xls", "application/vnd.ms-excel");
        MAP_EXT2MIME.put("ppt", "application/mspowerpoint");
        MAP_EXT2MIME.put("eps", "application/postscript");
        MAP_EXT2MIME.put("ps",  "application/postscript");

        MAP_EXT2MIME.put("swf", "application/x-shockwave-flash");
        MAP_EXT2MIME.put("e2e", "application/x-e2e");       // HRT-II
        MAP_EXT2MIME.put("cda-xml", "application/x-hl7-cda-level-one+xml");

        MAP_EXT2MIME.put("txt", "text/plain");
        MAP_EXT2MIME.put("html", MIME_HTML);
        MAP_EXT2MIME.put("htm", MIME_HTML);
        MAP_EXT2MIME.put("xml", "text/xml");

        MAP_EXT2MIME.put("mpg", "video/mpeg");
        MAP_EXT2MIME.put("mpeg", "video/mpeg");
        MAP_EXT2MIME.put("avi", "video/x-msvideo");

        MAP_EXT2MIME.put("mp2", "audio/mpeg");
        MAP_EXT2MIME.put("mp3", "audio/mpeg");

        //--------------------------------------------------------------

        MAP_MIME2EXT.put(MIME_JPEG, "jpg");
        MAP_MIME2EXT.put("image/jp2", "jp2");
        MAP_MIME2EXT.put("image/tiff", "tif");
        MAP_MIME2EXT.put(MIME_PNG, "png");
        MAP_MIME2EXT.put("image/bmp", "bmp");
        MAP_MIME2EXT.put("image/gif", "gif");

        MAP_MIME2EXT.put(MIME_PDF, "pdf");
        MAP_MIME2EXT.put("application/vnd.fdf", "fdf");
        MAP_MIME2EXT.put("application/vnd.adobe.xfdf", "xfdf");
        MAP_MIME2EXT.put("application/msword", "doc");
        MAP_MIME2EXT.put("application/vnd.ms-excel", "xls");
        MAP_MIME2EXT.put("application/mspowerpoint", "ppt");
        MAP_MIME2EXT.put("application/postscript", "ps");

        MAP_MIME2EXT.put("application/x-shockwave-flash", "swf");
        MAP_MIME2EXT.put("application/x-e2e", "e2e");
        MAP_MIME2EXT.put("application/x-hl7-cda-level-one+xml", "cda-xml");

        MAP_MIME2EXT.put(MIME_HTML, "html");
        MAP_MIME2EXT.put("text/plain", "txt");
        MAP_MIME2EXT.put("text/xml", "xml");

        MAP_MIME2EXT.put("video/mpeg", "mpg");
        MAP_MIME2EXT.put("video/x-msvideo", "avi");

        MAP_MIME2EXT.put("audio/mpeg", "mp3");
    }
}