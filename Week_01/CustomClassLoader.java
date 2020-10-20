import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CustomClassLoader extends ClassLoader {

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] classBytes;
        byte[] correctClassBytes = new byte[0];
        try {
            classBytes = Files.readAllBytes(Paths.get("Hello.xlass")); //读取文件字节
            correctClassBytes = new byte[classBytes.length];

            for(int i=0; i < classBytes.length; i++) {
                correctClassBytes[i] = (byte) (255 - classBytes[i]); //求得正确的字节
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return defineClass(name, correctClassBytes,0, correctClassBytes.length);
    }

    public static void main(String[] args) {
        try {
            Class<?> hello = new CustomClassLoader().findClass("Hello");
            Method method = hello.getMethod("hello");
            method.invoke(hello.newInstance());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
