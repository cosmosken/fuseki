package com.bu.dong.fuseki.asm;

import org.objectweb.asm.*;

import java.io.IOException;

public class AsmEnhancer {
    public static byte[] enhanceProcessMethod() throws IOException {
        // 定义原始类名和方法描述符
        String className = "UserService";
        String methodName = "process";
        String methodDesc = "(Ljava/lang/String;)V";

        // 创建 ClassWriter，生成增强后的字节码
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        ClassVisitor cv = new ClassVisitor(Opcodes.ASM9, cw) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc,
                                             String signature, String[] exceptions) {
                MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
                if (methodName.equals(name) && methodDesc.equals(desc)) {
                    return new MethodVisitor(Opcodes.ASM9, mv) {
                        @Override
                        public void visitCode() {
                            // 插入代码：System.out.println("Method start");
                            super.visitCode();
                            super.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
                            super.visitLdcInsn("Method start");
                            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
                        }

                        @Override
                        public void visitInsn(int opcode) {
                            // 在 RETURN 前插入代码：System.out.println("Method end");
                            if (opcode == Opcodes.RETURN) {
                                super.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
                                super.visitLdcInsn("Method end");
                                super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
                            }
                            super.visitInsn(opcode);
                        }
                    };
                }
                return mv;
            }
        };

        // 生成增强后的字节码
        ClassReader cr = new ClassReader(className);
        cr.accept(cv, ClassReader.EXPAND_FRAMES);
        return cw.toByteArray();
    }
}