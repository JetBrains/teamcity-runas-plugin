namespace JetBrains.runAs.Tests
{
	using JetBrains.runAs.IoC;

	using NUnit.Framework;

	using Shouldly;

	public class ContainerTest
	{
		[Test]
		public void ShouldRegisterAndResolve()
		{
			// Given
			var instance = CreateInstance();

			// When
			instance.Register<IInterface>(() => new Impl1());
			var impl = instance.Resolve<IInterface>();

			// Then			
			impl.ShouldBeOfType<Impl1>();
		}

		[Test]
		public void ShouldRegisterAndResolveWhenNamed()
		{
			// Given
			var instance = CreateInstance();

			// When
			instance.Register<IInterface>(() => new Impl1(), "inst1");
			var impl = instance.Resolve<IInterface>("inst1");

			// Then			
			impl.ShouldBeOfType<Impl1>();
		}

		[Test]
		public void ShouldRegisterAndResolveWhenHasCtorArg()
		{
			// Given
			var instance = CreateInstance();

			// When
			instance.Register<int, IInterface>(id => new Impl2(id));
			var impl = instance.Resolve<int, IInterface>(10);

			// Then			
			impl.ShouldBeOfType<Impl2>();
			impl.Id.ShouldBe(10);
		}

		[Test]
		public void ShouldRegisterAndResolveWhenNamedAndHasCtorArg()
		{
			// Given
			var instance = CreateInstance();

			// When
			instance.Register<int, IInterface>(id => new Impl2(id), "inst2");
			var impl = instance.Resolve<int, IInterface>(11, "inst2");

			// Then			
			impl.ShouldBeOfType<Impl2>();
			impl.Id.ShouldBe(11);
		}		

		[Test]
		public void ShouldResolveParentContainer()
		{
			// Given
			var instance = CreateInstance();

			// When
			instance.Register<IInterface>(() => new Impl1());
			instance.Register<IInterface>(() => new Impl1(), "abc");
			var parentContainer = instance.Resolve<IContainer>();

			parentContainer.Register<IInterface>(() => new Impl3());

			// Then			
			parentContainer.Resolve<IInterface>().ShouldBeOfType<Impl3>();
			parentContainer.Resolve<IInterface>("abc").ShouldBeOfType<Impl1>();
		}

		private IContainer CreateInstance()
		{
			return new Container();
		}

		private interface IInterface
		{
			int Id { get; }
		}

		private class Impl1: IInterface
		{
			public int Id {
				get
				{
					return 0;
				}
			}
		}

		private class Impl2 : IInterface
		{
			public Impl2(int id)
			{
				Id = id;
			}

			public int Id { get; private set; }
		}

		private class Impl3 : IInterface
		{
			public int Id
			{
				get
				{
					return 0;
				}
			}
		}
	}
}
