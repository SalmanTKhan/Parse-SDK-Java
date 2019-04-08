# Parse-SDK-Java
Parse SDK for Java ported from the [Parse SDK for Android](https://github.com/parse-community/Parse-SDK-Android)

A library that gives you access to the powerful Parse cloud platform from your Java application.
For more information about Parse and its features, see [the website](https://parseplatform.org/), [getting started][guide], and [blog](https://blog.parseplatform.org/).

### Setup
Initialize Parse:
```java
import android.content.Context;
import com.parse.Parse;
import com.parse.ParseObject;

public class Runner {
    public static void main(String[] args) {

        //Register your parseobject subclasses before usage.
        //ParseObject.registerSubclass(Post.class);
        
        Parse.initialize(new Parse.Configuration.Builder()
                .applicationId("YOUR_APP_ID")
                .masterKey("YOUR_MASTER_KEY") // Optional Master Key for Importing Data in Bulk
                .clientKey("YOUR_CLIENT_KEY")
                .server("http://localhost:1337/parse/") // Your server address
                .versionName("Parse JDK 0.1")
                .versionCode(1)
                .build()
        );
    }
}
```

See the [guide][guide] for the rest of the SDK usage.

## How Do I Contribute?
We want to make contributing to this project as easy and transparent as possible. Please refer to the [Contribution Guidelines](CONTRIBUTING.md).

## License
    Copyright (c) 2015-present, Parse, LLC.
    All rights reserved.

    This source code is licensed under the BSD-style license found in the
    LICENSE file in the root directory of this source tree. An additional grant
    of patent rights can be found in the PATENTS file in the same directory.

-----

As of April 5, 2017, Parse, LLC has transferred this code to the parse-community organization, and will no longer be contributing to or distributing this code.

 [guide]: http://docs.parseplatform.org/android/guide/
 [open-collective-link]: https://opencollective.com/parse-server
