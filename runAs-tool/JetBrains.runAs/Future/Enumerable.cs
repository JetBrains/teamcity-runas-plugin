namespace JetBrains.runAs.Future
{
	using System;
	using System.Collections;
	using System.Collections.Generic;

	internal static class Enumerable
	{
		private static readonly Dictionary<Type, object> EmptyEnumerable = new Dictionary<Type, object>();

		[NotNull]
		public static IEnumerable<T> Select<TSrc, T>([NotNull] IEnumerable<TSrc> src, [NotNull] Func<TSrc, T> selector)
		{
			if (src == null)
			{
				throw new ArgumentNullException("src");
			}

			if (selector == null)
			{
				throw new ArgumentNullException("selector");
			}

			foreach (var item in src)
			{
				yield return selector(item);
			}
		}

		[NotNull]
		public static IEnumerable<TSrc> Distinct<TSrc, T>([NotNull] IEnumerable<TSrc> src, [NotNull] Func<TSrc, T> keySelector)
		{
			if (src == null)
			{
				throw new ArgumentNullException("src");
			}

			if (keySelector == null)
			{
				throw new ArgumentNullException("keySelector");
			}

			var set = new Dictionary<T, object>();
			foreach (var item in src)
			{
				var key = keySelector(item);
				if (set.ContainsKey(key))
				{
					continue;
				}

				set.Add(key, null);
				yield return item;
			}
		}
		
		[NotNull]
		public static IEnumerable<TSrc> Where<TSrc>([NotNull] IEnumerable<TSrc> src, [NotNull] Func<TSrc, bool> filter)
		{
			if (src == null)
			{
				throw new ArgumentNullException("src");
			}

			if (filter == null)
			{
				throw new ArgumentNullException("filter");
			}

			foreach (var item in src)
			{
				if (filter(item))
				{
					yield return item;
				}
			}
		}

		[NotNull]
		public static IEnumerable<TSrc> Skip<TSrc>([NotNull] IEnumerable<TSrc> src, int count)
		{
			if (src == null)
			{
				throw new ArgumentNullException("src");
			}

			if (count < 0)
			{
				throw new ArgumentOutOfRangeException("count");
			}

			foreach (var item in src)
			{
				if(count <= 0)
				{
					yield return item;
				}
				else
				{
					count--;
				}
			}
		}

		[CanBeNull]
		public static TSrc FirstOrDefault<TSrc>([NotNull] IEnumerable<TSrc> src, [NotNull] Func<TSrc, bool> filter)
		{
			if (src == null)
			{
				throw new ArgumentNullException("src");
			}

			if (filter == null)
			{
				throw new ArgumentNullException("filter");
			}

			foreach (var item in Where(src, filter))
			{
				return item;
			}

			return default(TSrc);
		}

		[NotNull]
		public static List<TSrc> ToList<TSrc>([NotNull] IEnumerable<TSrc> src)
		{
			if (src == null)
			{
				throw new ArgumentNullException("src");
			}

			return new List<TSrc>(src);
		}

		[NotNull]
		public static TSrc[] ToArray<TSrc>([NotNull] IEnumerable<TSrc> src)
		{
			if (src == null)
			{
				throw new ArgumentNullException("src");
			}

			return new List<TSrc>(src).ToArray();
		}

		[NotNull]
		public static IEnumerable<TSrc> Cast<TSrc>([NotNull] IEnumerable src)
		{
			if (src == null)
			{
				throw new ArgumentNullException("src");
			}

			var enumerator = src.GetEnumerator();
			while (enumerator.MoveNext())
			{
				yield return (TSrc)enumerator.Current;
			}						
		}

		public static int Count<TSrc>([NotNull] IEnumerable<TSrc> src)
		{
			if (src == null)
			{
				throw new ArgumentNullException("src");
			}

			var counter = 0;
			using (var enumerator = src.GetEnumerator())
			{
				while (enumerator.MoveNext())
				{
					counter++;
				}
			}

			return counter;
		}		

		public static IEnumerable<T> Empty<T>()
		{
			object emptyEnumerableObj;
			IEnumerable<T> emptyEnumerable;
			if (EmptyEnumerable.TryGetValue(typeof(T), out emptyEnumerableObj))
			{
				emptyEnumerable = (IEnumerable<T>)emptyEnumerableObj;
			}
			else
			{
				emptyEnumerable = new List<T>().AsReadOnly();
				EmptyEnumerable.Add(typeof(T), emptyEnumerable);
			}

			return emptyEnumerable;
		}
	}
}
