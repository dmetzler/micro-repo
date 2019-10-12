Micro Repo
==========

DISCLAIMER
----------

Read this before anything else:

This repository holds a toy project that is not intended to be used and you can NOT expect that it will work for real one day. It's basically a research project to test new ideas. It currently doesn't even compile.


What is this? Why did I commit that?
------------------------------------

First of all, most of the code in this repo is copy/pasted from the [Nuxeo repository](https://github.com/nuxeo/nuxeo). You can think of it as a fork.

After playing with serverless technologies, I was wondering if we could start a Nuxeo Repository in an AWS Lambda... To do that I thought (and was perhaps wrong) that removing the Nuxeo Runtime layer could help in starting faster, and avoid the time to load the various needed extensions.

So I started by removing the Nuxeo Runtime and tried to replace every call to `Framework#getService` in order to build a Nuxeo `CoreSession`.

...

And it appeared that it was possible minus lots of tradeoff that I will describe in my [blog](https://dmetzler.github.io). It also emphasized some very cool opportunities and help me identify the dependencies between a lot of components in Nuxeo.


How to have a look at it?
-------------------------

Did I tell you that it doesn't compile?

The only way to currently see a few stuffs running is by importing the project into an IDE, start a local MongoDB and run the test(s).


Licensing
---------

Most of the source code in the Nuxeo Platform is copyright Nuxeo and
contributors, and licensed under the Apache License, Version 2.0.

See the [LICENSE](LICENSE) file and the documentation page [Licenses](http://doc.nuxeo.com/x/gIK7) for details.

About Nuxeo
-----------

Nuxeo dramatically improves how content-based applications are built, managed and deployed, making customers more agile, innovative and successful. Nuxeo provides a next generation, enterprise ready platform for building traditional and cutting-edge content oriented applications. Combining a powerful application development environment with SaaS-based tools and a modular architecture, the Nuxeo Platform and Products provide clear business value to some of the most recognizable brands including Verizon, Electronic Arts, Sharp, FICO, the U.S. Navy, and Boeing. Nuxeo is headquartered in New York and Paris. More information is available at [www.nuxeo.com](http://www.nuxeo.com).

