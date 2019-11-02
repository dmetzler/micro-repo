Micro Repo
==========

[![Build Status](https://travis-ci.org/dmetzler/micro-repo.svg?branch=master)](https://travis-ci.org/dmetzler/micro-repo)

DISCLAIMER
----------

Read this before anything else:

This repository holds a toy project that is not intended to be used and you can NOT expect that it will work for real one day. It's basically a research project to test new ideas. ~~It currently doesn't even compile~~



What is this? Why did I commit that?
------------------------------------

First of all, most of the code in this repo is copy/pasted from the [Nuxeo repository](https://github.com/nuxeo/nuxeo). You can think of it as a fork.

After playing with serverless technologies, I was wondering if we could start a Nuxeo Repository in an AWS Lambda... To do that I thought (and was perhaps wrong) that removing the Nuxeo Runtime layer could help in starting faster, and avoid the time to load the various needed extensions.

So I started by removing the Nuxeo Runtime and tried to replace every call to `Framework#getService` in order to build a Nuxeo `CoreSession`.

...

And it appeared that it was possible minus lots of tradeoff that I will describe in my [blog](https://dmetzler.github.io). It also emphasized some very cool opportunities and help me identify the dependencies between a lot of components in Nuxeo.


Can I test it?
--------------

As of November 2nd 2019, the project features a sample application that exposes documents of type `Library`. You can run it with `docker-compose` which will build the project and deploy it alongside a MongoDB instance.

First create a `config/application.yaml` file by using the provided template and update the OAuth properties (by [creating a OAuth app on Github](https://github.com/settings/developers)).

Then simply launch `docker-compose up` and once started, access [http://localhost:8080/graphiql/](http://localhost:8080/graphiql/).

Here are some sample requests that you can do:

```graphql
query all {
  allLibraries {
    id
    path
    creator
    city
    country
  }
}


mutation Library {
  newLibrary(name: "UCLA library", city: "Los Angeles", country: "USA") {
    id
  }
}

mutation deleteLibrary {
  deleteLibrary(id:"35cb3d0b-0160-4e8f-a8aa-258700c1826c") {
    id
  }
}
```

To hack the domain model, you can have a look at the `micro-library` module which is basically a [Vert.X](https://vertx.io/) application

Licensing
---------

Most of the source code in the Nuxeo Platform is copyright Nuxeo and
contributors, and licensed under the Apache License, Version 2.0.

See the [LICENSE](LICENSE) file and the documentation page [Licenses](http://doc.nuxeo.com/x/gIK7) for details.

About Nuxeo
-----------

Nuxeo dramatically improves how content-based applications are built, managed and deployed, making customers more agile, innovative and successful. Nuxeo provides a next generation, enterprise ready platform for building traditional and cutting-edge content oriented applications. Combining a powerful application development environment with SaaS-based tools and a modular architecture, the Nuxeo Platform and Products provide clear business value to some of the most recognizable brands including Verizon, Electronic Arts, Sharp, FICO, the U.S. Navy, and Boeing. Nuxeo is headquartered in New York and Paris. More information is available at [www.nuxeo.com](http://www.nuxeo.com).

