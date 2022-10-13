# AWS::Synthetics::NoCodeCanary HttpConfig

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#port" title="Port">Port</a>" : <i>Integer</i>,
    "<a href="#hostname" title="Hostname">Hostname</a>" : <i>String</i>,
    "<a href="#httpmethod" title="HttpMethod">HttpMethod</a>" : <i>String</i>,
    "<a href="#httpheaders" title="HttpHeaders">HttpHeaders</a>" : <i>[ <a href="httpconfig-httpheaders.md">HttpHeaders</a>, ... ]</i>,
    "<a href="#httpassertions" title="HttpAssertions">HttpAssertions</a>" : <i>[ <a href="httpassertion.md">HttpAssertion</a>, ... ]</i>,
    "<a href="#useipv6" title="UseIPv6">UseIPv6</a>" : <i>Boolean</i>,
    "<a href="#requestbody" title="RequestBody">RequestBody</a>" : <i>String</i>,
    "<a href="#verifycert" title="VerifyCert">VerifyCert</a>" : <i>Boolean</i>
}
</pre>

### YAML

<pre>
<a href="#port" title="Port">Port</a>: <i>Integer</i>
<a href="#hostname" title="Hostname">Hostname</a>: <i>String</i>
<a href="#httpmethod" title="HttpMethod">HttpMethod</a>: <i>String</i>
<a href="#httpheaders" title="HttpHeaders">HttpHeaders</a>: <i>
      - <a href="httpconfig-httpheaders.md">HttpHeaders</a></i>
<a href="#httpassertions" title="HttpAssertions">HttpAssertions</a>: <i>
      - <a href="httpassertion.md">HttpAssertion</a></i>
<a href="#useipv6" title="UseIPv6">UseIPv6</a>: <i>Boolean</i>
<a href="#requestbody" title="RequestBody">RequestBody</a>: <i>String</i>
<a href="#verifycert" title="VerifyCert">VerifyCert</a>: <i>Boolean</i>
</pre>

## Properties

#### Port

_Required_: No

_Type_: Integer

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Hostname

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### HttpMethod

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### HttpHeaders

_Required_: No

_Type_: List of <a href="httpconfig-httpheaders.md">HttpHeaders</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### HttpAssertions

_Required_: No

_Type_: List of <a href="httpassertion.md">HttpAssertion</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### UseIPv6

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### RequestBody

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### VerifyCert

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

