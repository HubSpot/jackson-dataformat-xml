Here are people who have contributed to development of this project:
(version numbers in brackets indicate release in which the problem was fixed)

Tatu Saloranta, tatu.saloranta@iki.fi: author

Sebastien Dionne:

* Suggested Issue-23: Add @JacksonXmlText annotation (alias for JAXB @XmlValue),
  to support case of property values as 'unwrapped' text
 (2.0.1)

Pascal Gelinas:

* Reported and fixed #84: Problem with @JacksonXmlText when property output is suppressed
 (2.3.1)
* Reported and fixed #83: Add support for @JsonUnwrapped
 (2.4.0)

Dan Jasek: (oillio@github)

* Contributed #126: Allow specifying properties that should be written as CData
 (2.5.0)

Leo Wang (wanglingsong@github)

* Reported #171: `@JacksonXmlRootElement` malfunction in multi-thread environment
 (2.6.4)

Yury Vasyutinskiy (Falland@github)

* Contributed #232: Implement `writeRawValue` in `ToXmlGenerator`
 (2.9.0)

Victor Khovanskiy (khovanskiy@githib)

* Reported #242: Deserialization of class inheritance depends on attributes order
 (2.10.0)

Nelson Dionisi (ndionisi@github)

* Reported #333: `OutputDecorator` not called with `XmlMapper`
 (2.10.0)

kevindaub@github.com:

* Reported, contributed fix for #336: WRITE_BIGDECIMAL_AS_PLAIN Not Used When Writing Pretty
 (2.10.0)

Sam Smith (Oracle Security Researcher)

* Reported #350: Wrap Xerces/Stax (JDK-bundled) exceptions during parser initialization
 (2.10.0)

Rohit Narayanan (rohitnarayanan@github)

* Reported #351: XmlBeanSerializer serializes AnyGetters field even with FilterExceptFilter
 (2.10.0)

Luke Korth (lkorth@github.com)

* Reported #366: XML containing xsi:nil is improperly parsed
 (2.10.2)

Martin Vysny (mvysny@github)

* Reported #395: Namespace repairing generates xmlns definitions for xml: prefix (which is implicit)
 (2.10.5)

Alexei Volkov (softkot@github)

* Reported #294: XML parser error with nested same element names
 (2.11.1)

Eric Schoonover (spoon16@github)

* Reported #86: Can not deserialize unwrapped list when `@JacksonXmlProperty` localName
  matches `@JacksonXmlRootElement` localName
 (2.12.0)

Denis Chernyshov (danblack@github)

* Reported #124: Deserialization if an empty list (with empty XML tag) results in `null`
 (2.12.0)

Julien Debon (Sir4ur0n@github)

* Reported #252: Empty (or self-closing) Element representing `List` is incorrectly
  deserialized as null, not Empty List
 (2.12.0)

Dave Jarvis (DaveJarvis@github)

* Requested #262: Make `ToXmlGenerator` non-final
 (2.12.0)

Joseph Petersen (jetersen@github)

* Reported #273: Input mismatch with case-insensitive properties
 (2.12.0)

Eduard Wirch (ewirch@github)

* Reported #314: Jackson gets confused by parent list element
 (2.12.0)

Jochen Schalanda (joschi@github)

* Reported #318: XMLMapper fails to deserialize null (POJO reference) from blank tag
 (2.12.0)

Ghenadii Batalski (ghenadiibatalski@github)

* Reported #377: `ToXmlGenerator` ignores `Base64Variant` while serializing `byte[]`
 (2.12.0)

David Schmidt (d-schmidt@github)

* Reported #390: Unexpected attribute at string fields causes extra objects to be
  created in parent list
 (2.12.0)
