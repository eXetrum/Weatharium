package com.exsoft.weatharium.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Created by eXetrum on 02.12.2015.
 */

public class InputStreamUtils {

    public static String toString(InputStream istream) {
        Writer writer = new StringWriter();
        char[] buffer = new char[8192];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(istream, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } catch (Exception e) {
        } finally {
            try {
                istream.close();
            } catch (Exception e) {}
        }
        return writer.toString();
    }

    public static ByteArrayInputStream toByteStream(InputStream istream) {
        ByteArrayInputStream byteistream = new ByteArrayInputStream(new byte[0]);
        try {
            ByteArrayOutputStream byteostream = new ByteArrayOutputStream(8192);
            byte[] buffer = new byte[8192];
            int lenght;
            while ((lenght = istream.read(buffer)) != -1) {
                byteostream.write(buffer, 0, lenght);
            }
            byteistream = new ByteArrayInputStream(byteostream.toByteArray());
            byteostream.close();
        } catch (Exception e) {
        } finally {
            try {
                istream.close();
            } catch (Exception e) {}
        }
        return byteistream;
    }

    public static byte[] toByteArray(InputStream istream) {
        ByteArrayOutputStream byteostream = null;
        try {
            byteostream = new ByteArrayOutputStream(8192);
            byte[] buffer = new byte[8192];
            int lenght;
            while ((lenght = istream.read(buffer)) != -1) {
                byteostream.write(buffer, 0, lenght);
            }
            return byteostream.toByteArray();
        } catch (Exception e) {
            return null;
        } finally {
            try {
                istream.close();
                byteostream.close();
            } catch (Exception e) {}
        }
    }

}
