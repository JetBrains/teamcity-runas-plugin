namespace JetBrains.runAs.Future
{
	using System;

	internal class Lazy<T>
	{
		private readonly Func<T> _valueProvider;
		private bool _hasValue;
		private T _value;

		public Lazy([NotNull] Func<T> valueProvider)
		{
			if (valueProvider == null)
			{
				throw new ArgumentNullException("valueProvider");
			}

			_valueProvider = valueProvider;
		}

		[CanBeNull]
		public T Value
		{
			get
			{
				if (!_hasValue)
				{
					_value = _valueProvider();
					_hasValue = true;					
				}
				
				return _value;
			}
		}
	}
}
