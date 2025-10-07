
## Documentation

- README.md describes the project and how to build it.

## Coding Conventions

- When creating a git branch name use '_' in place of spaces in the name
- Try to inline single line methods that have no anonymous classes
- Remove empty lines in import section except for between standard imports and static imports
- Else/catch/etc... goes on new line
- Only wrap lines significantly longer then 120 characters.
- When implementing a method, check if a similar method already exist, if it does, use it, if its private make it public
- When implementing custom jackson json deserializer, loop the parser, then create the java object, Only 'readTree' if you specifically need the tree
- Create a util classes only when there is 2 or more util method that needs grouping, otherwise just add it to an appropriate existing class

## Testing

- Tests should be junit tests, use real objects for dependencies whenever possible, only mock if absolutely necessary.
- when creating objects always try and use public constructors and setters, only use reflection when absolutely necessary.

## Common Agent mistakes

- When not sure about public methods of a class, do not guess, find that class online to see its public methods

