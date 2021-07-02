package ingenious.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;


public class MyUtils {
	public static String fileToString(String file) throws IOException {
		InputStream resourceAsStream = MyUtils.class.getClassLoader().getResourceAsStream(file);
		return IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8.name());
	}

}
