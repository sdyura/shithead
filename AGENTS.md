
## Documentation

- README.md describes the project and how to build it.

## Coding Conventions

- avoid creating a method for a simple 1 line with no anonymous classes, just inline that line
- no empty lines in import section except for between standard imports and static imports
- else/catch/etc... goes on new line
- avoid wrapping long lines
- when implementing a method, check a similar method does not already exist, if it does, use it, if its private make it public
- when using jackson to convert json into a java object, do NOT 'readTree', loop the parser, then create the java object
- avoid creating a util class for every util method, just add it to an appropriate existing class, only when there are more then 2 methods that need to be grouped together create the util class

## Testing

- Tests should be junit tests, avoid using mock objects for dependencies whenever possible.

## Common Agent mistakes

- when not sure about public methods of a class, do not guess, find that class online to see its public methods

