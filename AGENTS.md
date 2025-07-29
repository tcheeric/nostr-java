# Repo Guidelines

## Description
nostr-java is a java implementation of the nostr protocol. The specification is available on github, here: https://github.com/nostr-protocol/nips
The URL format for the NIPs is https://github.com/nostr-protocol/nips/blob/master/XX.md where XX is the NIP number. For example, the specification for NIP-01 is available at the URL https://github.com/nostr-protocol/nips/blob/master/01.md etc.


## Testing

- Always run `mvn -q verify` from the repository root before committing your changes.
- Include the command's output in the PR description.
- If tests fail due to dependency or network issues, mention this in the PR.
- Update the `README.md` file if you add or modify features.
- Update the `pom.xml` file for new modules or dependencies, ensuring compatibility with Java 21.
- Add unit tests for new functionality, covering edge cases.
- Ensure modifications to existing code do not break functionality and pass all tests.
- Add integration tests for new features to verify end-to-end functionality.
- Ensure new dependencies or configurations do not introduce security vulnerabilities.
- Maintain the versions in the configuration section of the pom.xml files.
- Always make sure that the events are compliant with the Nostr protocol specifications, and that the events are valid according to the NIP specifications.

## Pull Requests

- Summarize the changes made and describe how they were tested.
- Include any limitations or known issues in the description.
- Add a "Network Access" section summarizing blocked domains if network requests were denied.
- Ensure all new features, modules, or dependencies are properly documented in the `README.md` file.