
About
-----
The Safe Exam Browser Server web application simplifies and centralizes the configuration of Safe Exam Browser clients for exams. It interacts with a learning management or exam system for setting up and conducting e-assessments with Safe Exam Browser. It also improves security by allowing to monitor connected Safe Exam Browser clients in real time during e-assessments. 

What is Safe Exam Browser (SEB)?
--------------------------------

`Safe Exam Browser <https://safeexambrowser.org/>`_ (SEB) is an application to carry out e-assessments safely. The freeware application is available for Windows, macOS and iOS. It turns any computer temporarily into a secure workstation. It controls access to resources like system functions, other websites and applications and prevents unauthorized resources being used during an exam. Safe Exam Browser can work with Open edX to control what a student can access during a Open edX quiz attempt. With the SEB Open edX plugin you activate the SEB support in Open edX and now only students using an approved version of SEB and the correct settings will be able to access the quiz in your Open edX course. The Safe Exam Browser is offered under a Mozilla Public License and supported by the `SEB Alliance <https://safeexambrowser.org/alliance/>`_.


What is Safe Exam Browser Server (SEB Server)?
----------------------------------------------

While the interaction with SEB is well known in Learning Management Systems (LMS) like `Open edX <https://open.edx.org/>`_, 
`Moodle <https://moodle.org/>`_ etc. the SEB Server is an entirely new component to set up secured online exams. 
It interacts with the assessments system/LMS as well as with SEB on exam clients.It supports exam scenarios on student owned devices (BYOD) 
and on managed devices.

SEB Server is a modern webservice with a REST API and a GUI service on top of it. SEB Server is written in Java and uses Docker for installation and setup.

SEB Server provides a range of basic functionalities:

- Built-in institutional multitenancy 
- Linking of multiple Learning Management Systems (LMS). Currently supported: `Open edX <https://open.edx.org/>`_
- Accessing the Course/Exam-API of a linked LMS to import a courses or exams for managing with SEB Server
- Creation and administration of SEB Client Configurations that can be used to startup a SEB and that contains SEB Server connection information for a SEB Client
- Creation and administration of SEB Exam Configurations that can be bound to an imported Exam to automatically configure a SEB Client that connects to an exam that is managed by SEB Server
- Automated SEB restriction on LMS side if the specified type of LMS supports the SEB restriction API
- Monitoring and administration of SEB Client connections within a running exam

The image below shows a very simplified diagram that locates the SEB Server in a setup with a Learning Management System (LMS) and the 
Safe Exam Browser (SEB). The SEB Server communicates with the LMS for managing and prepare exams as well as with the SEB Client to ensure 
a more automated and secure setup for high-stake exams.

.. image:: https://raw.githubusercontent.com/SafeExamBrowser/seb-server/master/docs/images/seb-sebserver-lms.png
    :align: center
    :target: https://raw.githubusercontent.com/SafeExamBrowser/seb-server/master/docs/images/seb-sebserver-lms.png
    

SEB Server Version 1.3.0 is out
-------------------------------

New features:

- Table Filter: Enter input on filter input field triggers filter action
- New: "Exam Templates" to predefine exam configurations that can be applied on exam import
- Exam Configuration: Copy exam configuration on exam configuration table view
- Exam Templates: Add an exam configuration template via exam template to automatically create an exam configuration on import
- Exam Templates: Add indicators for exam template that are automatically applied on exam import
- Monitoring: Add new filter to filter active connections (without any incidences)
- Monitoring: Add connection summary for each connection state filter to show how many connections are present per state
- Monitoring: Improved and extended connection information about user/login change and display client info like SEB version, OS Version...
- Monitoring: Improved distributed setup with Docker/Kubernetes
- Monitoring Notifications: Added Raise-Hand Notification and SEB Lock-Screen Notification


Bugfixes:

- Various distributed setup cache issues
- Fixed Sorting of exam in exam list
- Fixed LMS Lockup quizzes filter
- Fixed Exam proctoring settings verification
- Various proctoring issues for proctoring with Zoom


Changes:

- Overall: Extended GUI server session timeout
- User Roles: Enhanced "Exam Administrator" role to see all running exams and be able to support them as well
- LMS Lockup/Exam: Show Moodle course name together with the quiz name on LMS lockup as well as on exams
- Exam Configuration: Streamline "Exam Configuration" settings with the newest SEB versions
- Monitoring: Improved indicator and monitoring data performance for distributed setups
- Monitoring: Changed default colors for active connections and indicators (No color if no incidence)

Docker-Image:

- Exact version: docker pull anhefti/seb-server:v1.3.0 (sha256:35692e304ab8f7d198524ff948df472e1eb362f1eb7f0b0fa358d01556011e59)
- Latest stable minor version with patches: docker pull anhefti/seb-server:v1.3-latest


