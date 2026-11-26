# Third-party notices

Meridian's experimental JVM desktop application image currently includes SLF4J API 2.0.19, transitively through its runtime dependency graph. The following license text is reproduced from `META-INF/LICENSE.txt` inside the distributed `slf4j-api-2.0.19.jar`.

## SLF4J API 2.0.19

Project: https://www.slf4j.org/  
Source and license information: https://www.slf4j.org/license.html  
License: MIT

```text
Copyright (c) 2004-2023 QOS.ch
All rights reserved.

Permission is hereby granted, free  of charge, to any person obtaining
a  copy  of this  software and  associated documentation files (the
"Software"), to  deal in  the Software without  restriction, including
without limitation  the rights  to use, copy, modify,  merge, publish,
distribute,  sublicense, and/or sell  copies of  the Software,  and to
permit persons to whom the Software  is furnished to do so, subject
to the following conditions:

The  above copyright  notice and  this permission  notice shall be
included in all copies or substantial portions of the Software.

THE  SOFTWARE IS  PROVIDED  "AS IS", WITHOUT  WARRANTY OF ANY  KIND,
EXPRESS OR  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY,    FITNESS    FOR    A PARTICULAR    PURPOSE    AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
```

This file records the license text found in the exact CI package artifact, rather than substituting a newer upstream copyright year. Re-run package inspection and update this inventory whenever shipped dependencies change.


## SQLite JDBC 3.53.4.0 and bundled SQLite native libraries

The inspected desktop application images contain org.xerial:sqlite-jdbc:3.53.4.0. The JAR bundles native SQLite JDBC libraries for multiple operating systems and architectures, including Linux, macOS, and Windows. Its embedded Maven metadata declares Apache License 2.0, and the exact JAR also contains the additional license text below. Preserve all of these notices when redistributing this JAR.

Project: https://github.com/xerial/sqlite-jdbc  
License declared in embedded POM: Apache-2.0  
Additional embedded license: BSD-style license for the Zentus JDBC implementation  
Bundled SQLite engine: SQLite is in the public domain; see https://sqlite.org/copyright.html.

### Zentus JDBC implementation

The following text is reproduced from META-INF/maven/org.xerial/sqlite-jdbc/LICENSE.zentus inside the inspected sqlite-jdbc-3.53.4.0.jar.

```text
Copyright (c) 2006, David Crawshaw.  All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

1. Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in
   the documentation and/or other materials provided with the
   distribution.

THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.
```

The Apache-2.0 license text is provided in the distribution's LICENSE file. The SQLite public-domain dedication is not a restrictive third-party license; this notice records the bundled engine provenance.

## Runtime dependency inventory observed in the desktop packages

The following third-party runtime JARs were present in both inspected desktop app-image artifacts. The project-owned engine-desktop.jar and desktop-consumer.jar are not third-party dependencies.

- JetBrains annotations 23.0.0 — Apache-2.0.
- Kotlin standard library 2.4.21 — Apache-2.0.
- Kotlin coroutines core and SLF4J integration 1.11.0 — Apache-2.0.
- Kotlin I/O bytestring and core 0.9.1 — Apache-2.0.
- Kotlin serialization core 1.11.0 — Apache-2.0.
- Ktor 3.6.1 client, CIO, events, HTTP, IO, network, TLS, serialization, SSE, utilities and WebSocket modules — Apache-2.0.
- SQLDelight 2.4.1 runtime, JDBC driver and SQLite driver — Apache-2.0.
- SLF4J API 2.0.19 — MIT; exact license text is reproduced above.
- Xerial SQLite JDBC 3.53.4.0 — Apache-2.0, with the additional Zentus BSD-style text and bundled SQLite public-domain code.

The app-image bundles a Java runtime. Its runtime/legal directory contained 225 legal/attribution files in each inspected artifact. These include the applicable OpenJDK license and component notices; do not strip this directory from a redistributed app image.
