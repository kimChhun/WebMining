package ch.heigvd.wem.tools;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;

public class Tools {

	/**
	 * Method used to compute the hash (SHA-1) of a String
	 * 
	 * @param content
	 *            The content to hash (String)
	 * @return The SHA-1 hash of the content, hexadecimal String
	 * @throws Exception
	 */
	public static String hash(String content) {
		return hashToString(computeHash(content));
	}

	private static byte[] computeHash(String x) {
		try {
			MessageDigest d = MessageDigest.getInstance("SHA-1");
			d.update(x.getBytes(Charset.forName("UTF8")));
			return d.digest();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	private static String hashToString(byte[] hash) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < hash.length; ++i) {
			int v = hash[i] & 0xFF;
			if (v < 16) {
				sb.append("0");
			}
			sb.append(Integer.toString(v, 16));
		}
		return sb.toString();
	}

	public static class ReverseMapEntryComparator<K, V> implements Comparator<Map.Entry<K, V>> {
		public ReverseMapEntryComparator(Class<K> keyClass, Class<V> valueClass) {
			if (!Comparable.class.isAssignableFrom(valueClass)) throw new IllegalArgumentException("The value class must be comparable");
			Class<?> comparableType = null;
			for (Type type : valueClass.getGenericInterfaces()) {
				String typeName = type.getTypeName().replaceAll("<.*>", "");
				String comparableName = ((Type)Comparable.class).getTypeName();
				if (!comparableName.equals(typeName)) continue;
				ParameterizedType clazz = (ParameterizedType) type;
				if (clazz.getActualTypeArguments().length == 1)
					if (clazz.getRawType() instanceof Class)
						comparableType = (Class<?>)clazz.getRawType();
			}
			if (comparableType == null) throw new IllegalStateException("could not find comparable type");
			if (!comparableType.isAssignableFrom(valueClass)) throw new IllegalArgumentException("The value class must be comparable to itself");
		}

		@SuppressWarnings("unchecked")
		@Override
		public int compare(Entry<K, V> o1, Entry<K, V> o2) {
			return -((Comparable<V>) o1.getValue()).compareTo(o2.getValue());
		}
	}
}
