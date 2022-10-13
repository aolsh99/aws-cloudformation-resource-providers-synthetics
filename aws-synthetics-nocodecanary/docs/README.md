# AWS::Synthetics::NoCodeCanary

Resource Type definition for AWS::Synthetics::NoCodeCanary

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::Synthetics::NoCodeCanary",
    "Properties" : {
        "<a href="#name" title="Name">Name</a>" : <i>String</i>,
        "<a href="#schedule" title="Schedule">Schedule</a>" : <i><a href="schedule.md">Schedule</a></i>,
        "<a href="#endpointlist" title="EndpointList">EndpointList</a>" : <i>[ String, ... ]</i>,
        "<a href="#state" title="State">State</a>" : <i>String</i>,
        "<a href="#nocodecanaryconfig" title="NoCodeCanaryConfig">NoCodeCanaryConfig</a>" : <i><a href="nocodecanaryconfig.md">NoCodeCanaryConfig</a></i>,
        "<a href="#tags" title="Tags">Tags</a>" : <i>[ <a href="tag.md">Tag</a>, ... ]</i>,
        "<a href="#startnocodecanaryaftercreation" title="StartNoCodeCanaryAfterCreation">StartNoCodeCanaryAfterCreation</a>" : <i>Boolean</i>
    }
}
</pre>

### YAML

<pre>
Type: AWS::Synthetics::NoCodeCanary
Properties:
    <a href="#name" title="Name">Name</a>: <i>String</i>
    <a href="#schedule" title="Schedule">Schedule</a>: <i><a href="schedule.md">Schedule</a></i>
    <a href="#endpointlist" title="EndpointList">EndpointList</a>: <i>
      - String</i>
    <a href="#state" title="State">State</a>: <i>String</i>
    <a href="#nocodecanaryconfig" title="NoCodeCanaryConfig">NoCodeCanaryConfig</a>: <i><a href="nocodecanaryconfig.md">NoCodeCanaryConfig</a></i>
    <a href="#tags" title="Tags">Tags</a>: <i>
      - <a href="tag.md">Tag</a></i>
    <a href="#startnocodecanaryaftercreation" title="StartNoCodeCanaryAfterCreation">StartNoCodeCanaryAfterCreation</a>: <i>Boolean</i>
</pre>

## Properties

#### Name

Name of the NoCodeCanary.

_Required_: Yes

_Type_: String

_Pattern_: <code>^[0-9a-zA-Z_\-]{1,255}$</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### Schedule

_Required_: No

_Type_: <a href="schedule.md">Schedule</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### EndpointList

_Required_: No

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### State

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### NoCodeCanaryConfig

_Required_: No

_Type_: <a href="nocodecanaryconfig.md">NoCodeCanaryConfig</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Tags

_Required_: No

_Type_: List of <a href="tag.md">Tag</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### StartNoCodeCanaryAfterCreation

Runs no-code canary if set to True. Default is False

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the Id.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### Id

Id of the NoCodeCanary

#### Arn

Arn of the NoCodeCanary

