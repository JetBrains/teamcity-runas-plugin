namespace JetBrains.runAs.IoC
{
	using System;
	using System.Collections.Generic;
	using System.Diagnostics;

	using JetBrains.runAs.Future;

	internal class Container : IContainer
	{
		private readonly Dictionary<string, object> _factories = new Dictionary<string, object>();
		[CanBeNull] private readonly Container _parentContainer;

		public Container()
		{
			Register<IContainer>(() => new Container(this));
		}

		private Container([NotNull] Container parentContainer)
			:this()
		{
			if (parentContainer == null)
			{
				throw new ArgumentNullException("parentContainer");
			}

			_parentContainer = parentContainer;
		}

		public IRegistry Register<T>(Func<T> factory)
		{
			return Register(new Func<EmptyArg, T>(arg => factory()));
		}

		public IRegistry Register<T>(Func<T> factory, string name)
		{
			return Register(new Func<EmptyArg, T>(arg => factory()), name);
		}

		public IRegistry Register<TArg, T>([NotNull] Func<TArg, T> factory)
		{
			if (factory == null)
			{
				throw new ArgumentNullException("factory");
			}

			return Register(factory, string.Empty);
		}
		
		public IRegistry Register<TArg, T>([NotNull] Func<TArg, T> factory, string name)
		{
			if (factory == null)
			{
				throw new ArgumentNullException("factory");
			}

			if (name == null)
			{
				throw new ArgumentNullException("name");
			}

			var key = CreateKey<TArg, T>(name);
			if (_factories.ContainsKey(key))
			{
				throw new InvalidOperationException(string.Format("The entry {0} was alredy registered", key));
			}

			_factories.Add(key, factory);
			return this;
		}

		public T Resolve<T>()
		{
			var service = Resolve<EmptyArg, T>(EmptyArg.Shared);
			Debug.Assert(service != null, "instance != null");
			return service;
		}

		public T Resolve<TArg, T>(TArg arg)
		{
			var service = Resolve<TArg, T>(arg, "");
			Debug.Assert(service != null, "instance != null");
			return service;
		}

		public T Resolve<T>(string name)
		{
			var service = Resolve<EmptyArg, T>(EmptyArg.Shared, name);
			Debug.Assert(service != null, "instance != null");
			return service;
		}

		public T Resolve<TArg, T>(TArg arg, string name)
		{
			if (name == null)
			{
				throw new ArgumentNullException("name");
			}

			var key = CreateKey<TArg, T>(name);
			object factory;
			if (_factories.TryGetValue(key, out factory))
			{
				var service = ((Func<TArg, T>)factory)(arg);
				Debug.Assert(service != null, "instance != null");
				return service;
			}

			if (_parentContainer != null)
			{
				return _parentContainer.Resolve<TArg, T>(arg, name);
			}

			throw new InvalidOperationException(string.Format("The entry {0} was not registered", key));
		}
					
		[NotNull]
		private static string CreateKey<TArg, T>([NotNull] string name)
		{
			if (name == null)
			{
				throw new ArgumentNullException("name");
			}

			return string.Format("{0}.{1}.{2}", typeof(T).FullName, typeof(TArg).FullName, name);
		}

		private class EmptyArg
		{
			public static readonly EmptyArg Shared = new EmptyArg();

			private EmptyArg()
			{
			}
		}
	}
}
