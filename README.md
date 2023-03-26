

# S3 recovery utility

## Building the project
Download the AWS toolkit for IntelliJ IDEA and connect your AWS account to IDEA. Then, run the following code

```
mvn clean package
mvn exec:java -Dexec.mainClass="executormode.App" 
```
The first command also runs the comprehensive JUnit test to validate the store/recover functioanlities.

## Description
This CLI tool allows the user to store local directories of any internal structure (i.e. empty, containing files, or even containing other subdirectories and files) as blobs inside an S3 bucket created in the user's AWS account specifically for the purposes of this utility.

The utility further allows the user to recover these blobs from the designated bucket and restore the folder in its original form from its binary representation in which it was stored in S3.

The utility puts great emphasis on user experience: selection of folders to push to S3 or folders to extract S3 files to is done via JavaSwing GUI file dialogue components while the selection of the command to conduct or the file to recover from the bucket is done by typing just one character.

Internally, the utility pushes a folder to S3 by first converting it to a zip file, converting the zip file to an array of bytes (a blob) and then pushing that blob to S3. The utility is then able to recover the folder's original contents by recovering the relevant blob from S3, converting it to a zip file and then decompressing this zip file in the folder that the user specified while also deleting the zip file to leave no traces of the internal workings of the utility.

To improve this functionality, the next step would probably be to implement functionality allow the user to implement a richer file system architecture inside the bucket that stores BLOBS, e.g., allowing them to make folders in this bucket. Another potential avenue I could explore would be to migrate from CLI and implementing a GUI for this utility, transforming it into a webapp. Furthermore, the app would benefit from further unit testing, which is made harder by the seeming presence of a good UI testing framework for JavaSwing. 

### Nevertheless, to test the main functionality of my app, I implemented a comprehensive JUnit test of the store and recover functionalities 
I did this by making debug versions of the recover and store handlers inside `UserInputOutputHandlerDebug.java` which together first upload a hard-coded newly created folder to the S3 bucket and then recover it from there in its original form. The test checks for whether the file does exist in local storage or on S3 when it should and that it doesn't when it shouldn't at all critical steps of the program's execution.

## Development

- `App.java`: main entry point and loop of the application
- `DependencyFactory.java`: creates the SDK client
- `S3ApiHandler.java`: contains helpers to communicate with the S3 bucket created to support the utility's functions.
- `UserInputOutputApiHandler.java`: contains helpers to process the users requests to either store in S3, recover from S3, print usage instructions, or exit
- `UserInputOutputApiHandlerDebug.java`: contains helpers used in the test of the recover/store functionalities of the utility. Separated from the parent `UserInputOutputApiHandler.java` file to keep testing and "production" code separate

