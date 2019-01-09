package com.fashare.javasuger.apt.test


import com.github.javaparser.JavaParser
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.visitor.VoidVisitorAdapter
import java.io.File
import java.io.FileInputStream



object TestJavaParser {
    /**
     * Simple visitor implementation for visiting MethodDeclaration nodes.
     */
    private class MethodChangerVisitor : VoidVisitorAdapter<Void>() {
        override fun visit(n: MethodDeclaration?, arg: Void?) {
            // change the name of the method to upper case
            n?.setName(n?.nameAsString.toUpperCase())

            // add a new parameter to the method
            n?.addParameter("int", "value")
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val dir = File("./libs/apt/src/main/kotlin/com/fashare/javasuger/apt/test/TestBean.java")

        printDir(dir)

        // creates an input stream for the file to be parsed
        val inputStream = FileInputStream(dir.absoluteFile)

        val cu = JavaParser.parse(inputStream)

        cu.accept(MethodChangerVisitor(), null)

        System.out.println(cu);
    }

    private fun printDir(dir: File) {
        if (dir.isDirectory) {
            dir.listFiles().forEach {
                printDir(it)
            }
        } else {
            println(dir.absolutePath)
        }
    }
}
