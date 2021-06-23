## Converter Number To Text
Project  for convert numbers into words.
A project to convert numbers to words.
The main logic is in the class NumberToWordsConverter in the convertServices module.
How it works: the number is split into an integer part and the fractional part then its parts are processed in parallel way.
Each part is recursively divided into groups of three digits.
The converter replaces the numbers in hundreds and tens units with the corresponding analogs, which it takes from the csv file, replacing the endings, if necessary.
