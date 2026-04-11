

## Project Pulse
Vision and Scope

## Version 1.0

[Note: Text enclosed in square brackets and displayed in blue italics is included to guide the author and should be
deleted before publishing the document.]


## Project Pulse
## Version: 1.0
## Vision   Date:  09/22/2024
<document identifier>

## Revision History
## Date Version Description Author






Confidential ©Bingyang Wei, 2024 Page 2



## Project Pulse
## Version: 1.0
## Vision   Date:  09/22/2024
<document identifier>

Table of Contents

## 1. Introduction 4
## 1.1 Background 4
1.2 Current Process Flow (As-Is Process Flow) 4
## 1.3 References 8
## 2. Business Requirements 9
2.1 Business Opportunity/Problem Statement 9
## 2.2 Business Objectives 9
## 2.3 Vision Statement 9
2.4 Proposed New Process Flow (To-Be Process Flow) 9
## 2.5 Business Risks 10
2.6 Business Assumptions and Dependencies 11
- Stakeholder Profiles and User Descriptions 12
## 3.1 Stakeholder Profiles 12
## 3.2 User Environment 12
3.3 Alternatives and Competition 12
- Scope and Limitations 14
## 4.1 Product Perspective 14
## 4.2 Major Features / Scope 14
## 4.3 Deployment Considerations 14



Confidential ©Bingyang Wei, 2024 Page 3



## Project Pulse
## Version: 1.0
## Vision   Date:  09/22/2024
<document identifier>

Vision and Scope
## 1. Introduction
This document defines the overarching goals, purpose, and boundaries of the software project. Its purpose is to
provide a shared understanding among stakeholders of what the software aims to achieve and the context in which it
will operate. It identifies the business problems the software will solve, outlines the future vision for how the
software will fit into the domain, and specifies the scope of the project by clearly stating what is included and
excluded. This document serves as a foundation for aligning the project team and stakeholders, guiding
decision-making, and managing expectations throughout the software development lifecycle. The details of how the
Project Pulse System fulfills these needs are detailed in the use cases and software requirements specification.

## 1.1 Background
[Summarize the rationale and context for the new product or for changes to be made to an existing one. Describe the
history or situation that led to the decision to build this product. Follow the steps:
Step 1: Describe the Business
● Introduce the company or organization.
## ● Include:
○ What the company does (industry, products/services).
○ The size of the company (number of employees, geographic locations, etc.).
○ The company’s mission or goals that relate to the problem domain.
## Example:
"The client, XYZ Logistics, is a mid-sized shipping company that specializes in last-mile delivery services for
e-commerce businesses. The company operates in five major cities, employs 200 delivery staff, and handles over
10,000 deliveries per day. The goal is to optimize delivery efficiency and customer satisfaction."]

The Department of Computer Science at Texas Christian University offers a course where senior students, in teams,
collaborate with clients to solve real-world software problems. Students handle every project phase: definition,
analysis, design, implementation, testing, deployment, and documentation. However, in a senior design team, there’s
a variation in how much each student contributes. Some are very active, while others are not. Communication issues
can also arise.
1.2 Current Process Flows (As-Is Process Flows)
## [
Most projects require the people involved to have a firm grasp of the business process being created, replicated, or
improved. Without this understanding, there is little chance that users will adopt the new solution. Process Flows are
among the most common and effective models to facilitate this understanding.

Step 2: Explain the Current Process Flows
● Create a UML Activity Diagram with Swimlanes to model the current business process steps people
execute. It shows the sequence of activities and decisions.
● Identify key actors or stakeholders (e.g., departments, roles, systems) and assign each one a swimlane.
● Map out the sequence of activities, decisions, and interactions between actors to visualize the existing
process.
● Write a textual explanation for readers unfamiliar with UML diagrams.
● Highlight pain points, inefficiencies, or problems in the current process flow to set the stage for
improvement.
● There may be multiple major process flows.




Confidential ©Bingyang Wei, 2024 Page 4



## Project Pulse
## Version: 1.0
## Vision   Date:  09/22/2024
<document identifier>

Tips for this Section:
● Break the process flow into logical steps (e.g., receiving an order, processing, delivery).
● Identify who is responsible for each step (e.g., customer, warehouse staff, delivery team, software).
● Mention the sequence of steps, decision points, and flow of information or materials.
● Download UMLet and use it to draw the activity diagram with swimlanes.

Step 3: Describe the Current Tools
● Enumerate the tools currently used in the process flow (e.g., Excel, paper schedules, legacy software).
● Mention the limitations of each tool.
## Example:
"XYZ Logistics relies heavily on Excel spreadsheets for order management. Printed delivery schedules are
distributed to drivers daily. These tools lack automation, making the process prone to human error and delays."

Step 4: Explain the Pain Points
● Highlight the inefficiencies, time-consuming tasks, or error-prone steps in the current process flow.
● Use one or two detailed examples to explain the problem clearly.
## Example:
Inefficiency Example: "The manual entry of order details into Excel often causes delays and transcription errors.
For instance, during peak seasons, order entries pile up, leading to delays in processing and delivery."
Time-Consuming Example: "Printing and distributing delivery schedules to drivers takes an additional 2 hours
daily, reducing time available for deliveries."

Step 5: Provide Context for the Reader
● Assume your audience knows nothing about the domain. Use simple language and define domain-specific
terms.
● Include visual aids (e.g., diagrams, flowcharts) to help explain the process.
## Checklist:
● Is the business context clear to someone unfamiliar with it?
● Does the process flow description provide step-by-step details?
● Are all stakeholders and tools adequately described?
● Are the inefficiencies illustrated with specific examples?
● Did I include a UML activity diagram with swimlanes?
## ]
To handle these challenges and improve team efficiency, there is a student performance tracking system in place.
This system has two main tools:
- Weekly Activity Report (WAR)
● Every student is required to complete a WAR per week.
● WAR records what a student has done for a week.
● Google Sheets is used for recording WARs (see the image below).
● Students in the same team can edit and view the shared Google Sheets document.
● Each sheet records the activities of a team in a week.
● Foster better communication among team members.

Confidential ©Bingyang Wei, 2024 Page 5



## Project Pulse
## Version: 1.0
## Vision   Date:  09/22/2024
<document identifier>


The WAR Google Spread Sheets is available here:
https://docs.google.com/spreadsheets/d/1jpxBQ8Gvv94bRl1gSpBzXRFxJu7IQXKjQZ5n28-EpD8/edit?usp=sharing

Here is the current process flow for submitting WARs:

Each Monday after the senior design project begins, every student must access the shared Google Sheets created for
their team, locate the appropriate sheet based on the week number, and document the activities they completed
during the previous week. The instructor will keep the URLs of all the Google Sheets and review the updated ones
on Tuesday. Finally, the instructor will grade and provide feedback to students through the university’s LMS
(Learning Management System: TCU Online).


Confidential ©Bingyang Wei, 2024 Page 6



## Project Pulse
## Version: 1.0
## Vision   Date:  09/22/2024
<document identifier>

## 2. Peer Evaluation Form
● Excel Spreadsheet is used to collect peer evaluations in a team for a week (see the image below).
● Increase students’ self-awareness and allow students to know how their team members perceive
them.

Here is the current process flow for submitting and grading the peer evaluations:

Each Tuesday after the senior design project begins, every student must first review the team’s WAR from the
previous week, complete the peer evaluation form in Excel Spreadsheet, and upload it to the university’s LMS. The
instructor will then download all the forms, run a Java program written by the instructor to parse the data, finalize
the grades, compile comments, and finally upload the grades and comments to the LMS. Students can subsequently
view their teammates’ evaluations through the same system.
While this system effectively improves team efficiency, it is overly manual and time-consuming. For the WAR, each
student must individually edit the Google Sheets document for the week, which is then reviewed by the instructor.
This process is prone to human error; for example, students may make mistakes when filling out the document,
potentially resulting in them not receiving proper credit.
Similarly, the Peer Evaluation process is cumbersome. Each student must review the WAR from the previous week,
create an Excel spreadsheet with specific columns, and upload it to TCU Online, the LMS used by TCU. Once all
students have submitted their peer evaluation reports, the instructor must manually download all the reports from
TCU Online, run them through a Java program to calculate results, and then re-upload the results to TCU Online.
This process flow not only introduces opportunities for errors (e.g., spreadsheets with incorrect formatting or
missing columns) but is also highly time-consuming for the instructor, who must handle multiple repetitive tasks
manually.
## 1.3 References
[This subsection provides a complete list of all documents referenced elsewhere in the Vision document. Identify
each document by title, report number if applicable, date, and publishing organization. Specify the sources from
which the references can be obtained. This information may be provided by reference to an appendix or to another
document.]


Confidential ©Bingyang Wei, 2024 Page 7



## Project Pulse
## Version: 1.0
## Vision   Date:  09/22/2024
<document identifier>

TODO: reference the WAR and Peer Evaluation form.



Confidential ©Bingyang Wei, 2024 Page 8



## Project Pulse
## Version: 1.0
## Vision   Date:  09/22/2024
<document identifier>

## 2. Business Requirements
[Projects are launched in the belief that creating or changing a product will provide worthwhile benefits for
someone and a suitable return on investment. The business requirements describe the primary benefits that the new
system will provide to its sponsors, buyers, and users. Input to the business requirements should come from people
who have a clear sense of why they are undertaking the project. These individuals might include the customer or
development organization’s senior management, a product visionary, a product manager, a subject matter expert, or
members of the marketing department. Business requirements directly influence which user requirements to
implement and in what sequence. So, take it seriously!]
2.1 Business Opportunity/Problem Statement
[Provide a statement summarizing the opportunity being exploited or problem being solved by this project. The
following format may be used:]

The current peer evaluation process within the Computer Science Department at TCU is inundated by inefficiencies,
errors, and delays. Students are burdened with the laborious task of downloading, completing, and uploading peer
evaluation forms on TCU Online, leading to a suboptimal academic experience. Faculty members struggle with the
manual management of evaluations, sometimes resulting in delayed feedback. This inefficiency and error-prone
method have substantial negative implications, hindering academic growth and resource allocation within the
department. Addressing this problem presents a significant business opportunity. By automating the peer evaluation
process, we can streamline operations, improve data accuracy, and provide timely feedback, all of which enhance the
educational experience. This solution optimizes resource allocation and offers a user-friendly experience for both
students and faculty. This result is a more efficient and effective peer evaluation system, benefitting the entire
Computer Science department and fostering academic growth.
## 2.2 Business Objectives
[Summarize the important business benefits the product will provide in a quantitative and measurable way.
Platitudes (“become recognized as a world-class <whatever>”) and vaguely stated improvements (“provide a more
rewarding customer experience”) are neither helpful nor verifiable.]

BO-1: Reduce the instructor’s time to grade the peer evaluation by 50%.
BO-2: Increase students’ WAR and Peer Evaluation submission rate by 20%.
BO-3: Reduce students’ time to finish the WAR and Peer Evaluation by 25%.
## 2.3 Vision Statement
[Provide an overall statement summarizing, at the highest level, the unique position the product intends to fill in the
marketplace. The following format may be used:]

For Students of TCU senior design
Who Need an easier way to submit and update weekly activity
reports and peer evaluations
The (product name) Project Pulse
That makes it easier for the student to submit weekly activity reports
and peer evaluations, and makes it easier for the instructors to
view and grade them
Unlike the traditional manual process
Our product streamlines the whole process making it more accessible and
painless for both the instructor and the students to submit and
grade progress.
2.4 Proposed New/Improved Process Flows (To-Be Process Flows)
[Design the New/Improved Process Flows:

Confidential ©Bingyang Wei, 2024 Page 9



## Project Pulse
## Version: 1.0
## Vision   Date:  09/22/2024
<document identifier>

- Create another UML Activity Diagram with Swimlanes to represent the IMPROVED process flow,
incorporating the software-to-be. Note some activities are automated system actions.
- Show how the software will interact with actors and streamline processes.
- Indicate how the proposed system addresses the identified issues, automates tasks, or introduces new
efficiencies.
- Label any significant changes or new activities introduced by the software.
- There may be multiple major process flows.
Goal: This step helps you envision how the software integrates into the domain and solves the identified problems.]
In this project, we will improve the current process flows.
The WAR process has not changed:

As shown in the improved process, both students and the instructor can now complete their tasks entirely within the
Project Pulse system. This streamlines the workflow, reducing manual steps and minimizing the risk of errors.
However, one manual task remains: the instructor must still upload grades to the university’s LMS. While this
integration would further enhance efficiency, it is currently outside the scope of this project.
The Peer Evaluation process is also simplified:

Confidential ©Bingyang Wei, 2024 Page 10



## Project Pulse
## Version: 1.0
## Vision   Date:  09/22/2024
<document identifier>


The improved process for submitting and grading weekly peer evaluations is illustrated in the UML activity
diagram. Students start by reviewing the WAR for their entire team and evaluating their team members within the
Project Pulse system. Once all evaluations are completed, Project Pulse automates the process by compiling peer
evaluations for the entire class, calculating scores, and generating feedback. The instructor can access and review the
aggregated scores and feedback directly in Project Pulse. Additionally, a student can independently view their scores
and feedback in the system. The final step, performed manually by the instructor, involves uploading the grades and
feedback to the university’s LMS to make them accessible to all students. This diagram emphasizes the seamless
interaction between participants, the significant automation achieved through Project Pulse, and the remaining
manual task of LMS integration, which is outside the current scope of this system.
## 2.5 Business Risks
[Summarize the major business risks associated with developing—or not developing—this product. Risk categories
include marketplace competition, timing issues, user acceptance, implementation issues, and possible negative
impacts on the business. Business risks are not the same as project risks, which often include resource availability
concerns and technology factors. Estimate the potential loss from each risk, the likelihood of it occurring, and any
potential mitigation actions.
## Example:
RI-1: The Cafeteria Employees Union might require that their contract be renegotiated to reflect the new employee
roles and cafeteria hours of operation. (Probability = 0.6; Impact = 3)
RI-2: Too few employees might use the system, reducing the return on investment from the system development and
the changes in cafeteria operating procedures. (Probability = 0.3; Impact = 9)
RI-3: Local restaurants might not agree to offer delivery, which would reduce employee satisfaction with the system
and possibly their usage of it. (Probability = 0.3; Impact = 3)
RI-4: Insufficient delivery capacity might not be available, which means that employees would not always receive
their meals on time and could not always request delivery for the desired times. (Probability = 0.5; Impact = 6).]

RI-1: If the system has to be deployed on a cloud service provider, the annual cloud fees need to be taken care of by
the Computer Science Department.
RI-2: The students’ peer evaluation database may be breached by hackers.
RI-3 The application could be more confusing to use than the previous method.
RI-4: The application might not be broad enough for other applications except for the TCU senior design course
specifically.

Confidential ©Bingyang Wei, 2024 Page 11



## Project Pulse
## Version: 1.0
## Vision   Date:  09/22/2024
<document identifier>

2.6 Business Assumptions and Dependencies
[An assumption is a statement that is believed to be true in the absence of proof or definitive knowledge. For
example, an assumption may state that a specific operating system will be available for the hardware designated for
the software product. If the operating system is not available, the Vision document will need to change. Record any
assumptions that the stakeholders made. Record any major dependencies the project has on external factors.
Examples are pending industry standards or government regulations, deliverables from other projects, third-party
suppliers, or development partners. Note the potential impact of an assumption not being true, or the impact of a
broken dependency, to help stakeholders understand why it is critical.
## Example:
AS-1: Systems with appropriate user interfaces will be available for cafeteria employees to process the expected
volume of meals ordered.
AS-2: Cafeteria staff and vehicles will be available to deliver all meals for specified delivery time slots within 15
minutes of the requested delivery time.
DE-1: If a restaurant has its own online ordering system, the Cafeteria Ordering System must be able to
communicate with it bi-directionally.]

AS-1: The system should use technologies that the client has knowledge about and can maintain after the product
has been delivered.



Confidential ©Bingyang Wei, 2024 Page 12



## Project Pulse
## Version: 1.0
## Vision   Date:  09/22/2024
<document identifier>

- Stakeholder Profiles and User Descriptions
[To effectively provide products and services that meet your stakeholders’ and users’ real needs it is necessary to
identify and involve all of the stakeholders as part of the Requirements Modeling process. You must also identify the
users of the system and ensure that the stakeholder community adequately represents them. This section provides a
profile of the stakeholders and users involved in the project, and the key problems that they perceive to be addressed
by the proposed solution. It does not describe their specific requests or requirements as these are captured in a
separate stakeholder requests artifact. Instead, it provides the background and justification for why the requirements
are needed.]

## 3.1 Stakeholder Profiles
Stakeholder Major value or benefit from
this product
Attitudes Major features of
interest
Constraints End user or not?
Students It will be easier for students to
submit and view their weekly
activities and peer evaluations.
Supportive Submitting everything in
one place without having
to download or upload
anything directly.
Students must be
instructed on how to
use the platform.
## Yes
Instructors It will be easier for instructors
to compile and grade the
students’ work and understand
the team dynamics.
Supportive View WARs and
generate peer evaluation
reports every week.
Instructors must be
instructed on how to
use the platform.
## Yes

## 3.2 User Environment
[Detail the working environment of the target user. Here are some suggestions:
## ●
Number of people involved in completing the task? Is this changing?
## ●
How long is a task cycle? Amount of time spent in each activity? Is this changing?
## ●
Any unique environmental constraints: mobile, outdoors, in-flight, and so on?
## ●
Which system platforms are in use today? Future platforms?
## ●
What other applications are in use? Does your application need to integrate with them?
This is where extracts from the Business Model could be included to outline the task and roles involved, and so on.]
Users will access the application using a web browser on their desktop, laptop, or mobile device
regardless of operating system.
3.3 Alternatives and Competition
[Identify alternatives the stakeholder perceives as available. These can include buying a competitor’s product,
building a homegrown solution, or simply maintaining the status quo. List any known competitive choices that exist
or may become available. Include the major strengths and weaknesses of each competitor as perceived by the
stakeholder or end user.]


Confidential ©Bingyang Wei, 2024 Page 13



## Project Pulse
## Version: 1.0
## Vision   Date:  09/22/2024
<document identifier>

The alternative is what is currently being used, which is where the student will upload their peer evaluation onto
TCU Online, and then the instructor will have to download each one and calculate the grade for each group
manually.




Confidential ©Bingyang Wei, 2024 Page 14



## Project Pulse
## Version: 1.0
## Vision   Date:  09/22/2024
<document identifier>

- Scope and Limitations
## 4.1 Product Perspective
[This subsection of the Vision document puts the product in perspective to other related systems and the user’s
environment. If the product is independent and totally self-contained, state it here. If the product is a component of a
larger system, then this subsection needs to relate how these systems interact and needs to identify the relevant
interfaces between the systems. One easy way to display the major components of the larger system,
interconnections, and external interfaces is with a context diagram.]


The Level 1: Context Diagram for the Project Pulse system provides a high-level overview of its
interactions with users and external systems. Project Pulse serves as the central platform,
enabling instructors to manage courses, create Weekly Activity Reports (WARs) and peer
evaluation templates, and review submissions, while students use it to submit WARs, complete
peer evaluations, and view scores and feedback. The system integrates with the Gmail system to
send automated email notifications, such as reminders and updates, to both instructors and
students. This diagram highlights the roles of the instructor and student as primary users, the
central functionality of Project Pulse, and its reliance on Gmail for communication, offering a
clear picture of the system’s operational scope and interactions.
## 4.2 Major Features / Scope
[List and briefly describe the major product features. Features are the high-level capabilities of the system that are
necessary to deliver benefits to the users and other stakeholders. To be more specific, each feature is an externally
desired service that typically requires a series of inputs to achieve the desired result. For example, a feature of a
problem tracking system might be the ability to provide trending reports.
Because the Vision document is reviewed by a wide variety of involved personnel, the level of detail needs to be
general enough for everyone to understand. However, enough detail must be available to provide the team with the
information they need to create a use-case model (In other words, use cases are derived from those features).
To effectively manage application complexity, we recommend for any new system, or an increment to an existing
system, capabilities be abstracted to a high enough level so 25-99 features result. These features provide the
fundamental basis for product definition, scope management, and project management. Each feature will be
expanded in greater detail in the use-case model.
Throughout this section, each feature will be externally perceivable by users, operators, or other interacting
external systems. These features should include a description of functionality and any relevant usability issues that
must be addressed. The following guidelines apply:

Confidential ©Bingyang Wei, 2024 Page 15



## Project Pulse
## Version: 1.0
## Vision   Date:  09/22/2024
<document identifier>

## ●
State features at the level of product capabilities
## ●
Keep feature descriptions at a general level. Provide 1 - 3 sentences per major feature.
## ●
Avoid detailed workflows, UI behavior, or algorithms.
● Avoid describing how the feature will be implemented.
## ●
Focus on capabilities needed and why (not how) they should be implemented.
## ●
Be understandable by a non-technical stakeholder (e.g., an external client)
## ]
FE-1: Manage senior design sections, teams, and students.
FE-3: Submit weekly activity reports and peer evaluations.
FE-4: Generate weekly activity reports and peer evaluation grades for the entire senior design
section.
See the use case document for more details.
## 4.3 Deployment Considerations
[Summarize the information and activities that are needed to ensure an effective deployment of the solution into its
operating environment. Describe the access that users will require to use the system, such as whether the users are
distributed over multiple time zones or located close to each other. State when the users in various locations need to
access the system. If infrastructure changes are needed to support the software’s need for capacity, network access,
data storage, or data migration, describe those changes. Record any information that will be needed by people who
will be preparing training or modifying business processes in conjunction with deployment of the new solution.]

The system will be hosted on a cloud service like Microsoft Azure.

Confidential ©Bingyang Wei, 2024 Page 16
