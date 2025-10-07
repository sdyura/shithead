
## Documentation

- README.md describes the project and how to build it.

## Coding Conventions

- when creating a git branch name use '_' in place of spaces in the name
- Try to inline single line methods what have no anonymous classes
- remove empty lines in import section except for between standard imports and static imports
- else/catch/etc... goes on new line
- only wrap lines significantly longer then 120 characters.
- when implementing a method, check if a similar method already exist, if it does, use it, if its private make it public
- when using jackson to convert json into a java object, do NOT 'readTree', loop the parser, then create the java object
- only create a util classes when there is 2 or more util method that needs grouping, otherwise just add it to an appropriate existing class

## Testing

- Tests should be junit tests, avoid using mock objects for dependencies whenever possible.

## Common Agent mistakes

- when not sure about public methods of a class, do not guess, find that class online to see its public methods

