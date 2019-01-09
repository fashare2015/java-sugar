package com.fashare.javasuger.apt.test;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by apple on 2019/1/10.
 */

public class TestJavaParser2 {
    public static void main(String[] args) throws Exception {
        File dir = new File("./libs/apt/src/main/kotlin/com/fashare/javasuger/apt/test/TestBean.java");

        printDir(dir);

        CompilationUnit cu = JavaParser.parse(new FileInputStream(dir));

        cu.accept(new MethodChangerVisitor(), null);

        System.out.println(cu);

        write(cu.toString(), dir);

    }

    private static void printDir(File dir) {
        if (dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                printDir(file);
            }
        } else {
            System.out.println(dir.getAbsolutePath());
        }
    }

    private static void write(String str, File file) {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(file));
            bw.write(str);
            bw.flush();
        } catch (Exception e) {

        } finally {
            if (bw != null)
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    /**
     * Simple visitor implementation for visiting MethodDeclaration nodes.
     */
    private static class MethodChangerVisitor extends VoidVisitorAdapter<Void> {
        @Override
        public void visit(MethodDeclaration n, Void arg) {
            // change the name of the method to upper case
            n.setName(n.getNameAsString().toUpperCase());

            // add a new parameter to the method
            n.addParameter("int", "value");
        }
    }
}
