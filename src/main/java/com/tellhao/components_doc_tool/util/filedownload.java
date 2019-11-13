package com.tellhao.components_doc_tool.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.Response;
import java.io.*;
import java.net.URLEncoder;


@Service
public class filedownload {
    @Value("${fileaddress}")
    private String fileaddress;



    public HttpServletResponse download1(String path, HttpServletResponse response) {
        try {
            // path是指欲下载的文件的路径。
            File file = new File(path);
            // 取得文件名。
            String filename = file.getName();
            // 取得文件的后缀名。
//            String ext = filename.substring(filename.lastIndexOf(".") + 1).toUpperCase();

            // 以流的形式下载文件。
            InputStream fis = new FileInputStream(path);
            int count = 0;
            while (count == 0) {
                count = fis.available();
            }
            byte[] buffer = new byte[count];
            fis.read(buffer);
            fis.close();

//            String a=new String(filename.getBytes());
            // 清空response
            response.reset();
            // 设置response的Header
            response.addHeader("Content-Disposition", "attachment;filename=" +URLEncoder.encode(filename, "UTF-8"));
//            response.addHeader("Content-Length", "" + file.length());
            OutputStream toClient =response.getOutputStream();
            response.setContentType("application/octet-stream");
            toClient.write(buffer);
//            toClient.flush();
            toClient.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return response;
    }


}


