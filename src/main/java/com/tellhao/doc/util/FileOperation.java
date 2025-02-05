package com.tellhao.doc.util;

import com.tellhao.doc.entity.FileTree;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


/**
 * @author: 韩聪寅
 * @create: 2019-11-27
 **/
@Service
public class FileOperation {
    private static final String SPOT = ".";
    private static final String COMMA = ",";
    @Value("${fileAddress}")
    private String fileAddress;


    /**
     * 取出目录下的所有文件-------------------------------------------------------------
     */
    public List<FileTree> getFileTree(String path, List<FileTree> list) {

        List<FileTree> tree = new ArrayList<FileTree>();
        FileTree fileTree = new FileTree();
        fileTree.setId("/");
        fileTree.setText("/");
        fileTree.setChildren(list);
        tree.add(fileTree);
        return tree;
    }

    public List<FileTree> getFileTree(String path, String extension) {
        List<FileTree> tree = new ArrayList<FileTree>();
        File trailDir = new File(path.trim());
        List<FileTree> treeNode = new ArrayList<FileTree>();
        for (File file : trailDir.listFiles()) {
            if (file.isFile()) {
                if (extensionFile(file.getName(), extension)) {
                      tree.add(setFileTree(file));
                }
            }
            if (file.isDirectory() && !file.getName().endsWith(".back")) {
                try {
                    treeNode = getFileTree(file.getCanonicalPath(), extension);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                tree.add(0, setFileTree(file,treeNode));

            }
        }
        return tree;

    }

    public List<FileTree> getFileTree(String path, int id) {
        List<FileTree> tree = new ArrayList<FileTree>();
        File trailDir = new File(path.trim());
        List<FileTree> treeNode = new ArrayList<FileTree>();
        for (File file : trailDir.listFiles()) {
            if (file.isFile()) {
                if (extensionFile(file.getName(), id)) {
                    tree.add(setFileTree(file));
                }
            } else if (file.isDirectory() && file.getName().endsWith(".back")) {
                try {
                    treeNode =getFileTree(file.getCanonicalPath(), 0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                tree.add(0, setFileTree(file,treeNode));
            } else {
                try {
                    treeNode =getFileTree(file.getCanonicalPath(), id);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (treeNode.size() > 0) {
                    tree.add(0,setFileTree(file,treeNode));
                }
            }
        }
        return tree;

    }

    public FileTree setFileTree(File file){
        FileTree fileTree=new FileTree();
        fileTree.setId(file.getName());
        fileTree.setText(file.getName());
        fileTree.setPid(file.getName());
        return fileTree;
    }
    public FileTree setFileTree(File file,List<FileTree> treeNode){
        FileTree fileTree=setFileTree(file);
        fileTree.setChildren(treeNode);
        fileTree.setUid(1);
        return fileTree;
    }




    public Boolean extensionFile(String file, int id) {
        if (id == 1) {
            return file.endsWith(".back") ? true : false;
        } else {
            return true;
        }
    }

    public Boolean extensionFile(String file, String extension) {
        if (!"".equals(extension)) {
            if (file.lastIndexOf(SPOT) > 0) {
                String suffix = file.substring(file.lastIndexOf(SPOT));
                int flag = 0;
                String[] strAry = extension.split(COMMA);
                for (int i = 0; i < strAry.length; i++) {
                    if (suffix.equals(strAry[i])) {
                        flag = 1;
                        break;
                    }
                }
                return flag == 1 ? true : false;
            } else {
                return false;
            }
        } else {
            return !file.endsWith(".back") ? true : false;
        }
    }

    /**
     * 删除文件
     */
    public int delFile(String delName, String address) {
        try {
            String path = fileAddress + address + delName;
            File file = new File(path);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            String date = dateFormat.format(new Date());
            if (file.exists()) {
                File back = new File(path + "." + date + ".back");
                return file.renameTo(back) ? 1 : 0;
            } else {
                return 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }


    public int trueDelFile(String delName, String address, int id) {
        //老师我想问这个方法，要不要分出两个子函数出来，然后这个函数判段用哪个，然后各自递归调用
        try {
            File file = new File(fileAddress + address + delName);
            String delFileName = "";
//            id=0,删除.back文件
            if (id == 0) {
                if (file.exists()) {
                    if (file.isFile()) {
                        file.delete();
                        return 1;
                    } else {
                        for (File delFile : file.listFiles()) {
                            if (delFile.isFile()) {
                                if (delFile.getName().endsWith(".back")) {
                                    delFile.delete();
                                }
                            } else {
                                delFileName = delName + "/" + delFile.getName();
                                trueDelFile(delFileName, address, id);
                            }
                        }
                        return 2;
                    }
                } else {
                    return 0;
                }

            } else {  //id！=0 删除文件夹下得所有文件（包括本身）
                for (File delFile : file.listFiles()) {
                    if (delFile.isFile()) {
                        delFile.delete();
                    } else {
                        delFileName = delName + "/" + delFile.getName();
                        trueDelFile(delFileName, address, id);
                    }
                    file.delete();
                }
                file.delete();
                return 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }


    }

    /**
     * 文件名的修改
     */
    public int renameFile(String path, String rename, String oldName) {
        String oldNamed = fileAddress + path + oldName;
        File oldFile = new File(oldNamed);
        String newName = fileAddress + path + rename;
        File refile = new File(newName);
        /*检查要重命名的文件是否存在，是否是文件*/
        if (!oldFile.exists() && refile.exists()) {
            return 0;
        } else {
            return oldFile.renameTo(refile) ? 1 : 0;
        }

    }

    /**
     *新建文件夹
     */
    public String newFile(String path, String fileName) {

        String address = fileAddress + path;
        int j = 1;
        File file = new File(address);
        String[] fileList = file.list();
        for (int i = 0; i < fileList.length; i++) {
            if (fileList[i].equals(fileName)) {
                fileName = "newFile" + j;
                j++;
                i = 0;
            }
        }
        File newFile = new File(address + "/" + fileName);
        newFile.mkdirs();
        return fileName;
    }

    /**
     * 文件的压缩
     */
    public String zip(String zipFileName, String sourceFileName) {
        System.out.println("压缩中...");
        ZipOutputStream out = null;
        try {
            //创建zip输出流
            out = new ZipOutputStream(new FileOutputStream(zipFileName));
            File sourceFile = new File(sourceFileName);
            out.setLevel(0);
            //调用函数
            compression(out, sourceFile, sourceFile.getName());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("压缩完成");
        return zipFileName;
    }

    public void compression(ZipOutputStream out, File sourceFile, String base) {
        /*如果路径为目录（文件夹）*/
        if (sourceFile.isDirectory()) {
            /*取出文件夹中的文件（或子文件夹）*/
            File[] fileList = sourceFile.listFiles();
            /*如果文件夹为空，则只需在目的地zip文件中写入一个目录进入点*/
            if (fileList.length == 0) {
                try {
                    out.putNextEntry(new ZipEntry(base + "/"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else /*如果文件夹不为空，则递归调用compress，文件夹中的每一个文件（或文件夹）进行压缩*/
            {
                for (int i = 0; i < fileList.length; i++) {
                    compression(out, fileList[i], base + "/" + fileList[i].getName());
                }
            }
            /*如果不是目录（文件夹），即为文件，则先写入目录进入点，之后将文件写入zip文件中*/
        } else {
            BufferedInputStream inputStream = null;
            FileInputStream fileIn = null;
            try {
                out.putNextEntry(new ZipEntry(base));
                fileIn = new FileInputStream(sourceFile);
                inputStream = new BufferedInputStream(fileIn);
                int count;
                /*将源文件写入到zip文件中*/
                while ((count = inputStream.read()) != -1) {
                    out.write(count);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    inputStream.close();
                    fileIn.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }


        }
    }

}
