void
PrimeFactors(int n)
{
  int count = 0;
  int temp = 0;
  int i = 0;
  for (i = 1; i <= n; i++)
    {
      int j = i - 1;
      while (j > 1)
        {
          if (i % j == 0) //Is PRIME
            break;
          else
            j--;
        }

      if (j == 1)
        {
          if (n % i == 0)
            {
              if (i > temp)
                temp = i;
              count++;
            }
        }
    }
}

int
main()
{
  int a = 35324;
  PrimeFactors(a);
  return 0;
}
