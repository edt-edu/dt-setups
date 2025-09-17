# Vorlage

## TITLE
### Story
- **Priority** (A,B,C): 
- **As** ROLE
- **I want to** DO_SOMETHING
- **So that I can*** ACHIVE_SOME_GOAL
### Acceptance Criteria
- **Given** that SOME-CONTEXT
- **When** SOME_ACTION_IS_CARRIED_OUT
- **Then** SOME_OBSERVABLE_OUTCOMES_SHOULD_OCCUR

# Roles
- **Original System (OS)**: The system to be twinned 
- **Operator**: Works with the Original System to achieve some goals
- **Deployer**: Installs and configures the digital twin system
- **Developer**: Software developer of the digital twin system
- **Engineer**: Creator of the Original System

# User Stories

## Timely representation
### Story
- **Priority** (A,B,C): A
- **As** Operator
- **I want to** observe certain properties P of the OS in the DT
- **So that I can*** make decisions about the OS
### Acceptance Criteria
- **Given that** the properties P can be digitally observed
- **When** I specify the desired observation frequency and precision 
- **Then** the DT is updated with this frequency and precision

## DT Model Extension
### Story
- **Priority** (A,B,C): B
- **As** Deployer
- **I want to** be able to use different models for the DT and update them automatically
- **So that I can*** satisfy the need for different views on the OS 
### Acceptance Criteria
- **Given that** I know the required modeling languages ML and have MontiCore parsers/writers for them 
- **When** create a DTS plugin with them and add it to the architecture 
- **Then** models of ML are also updated automatically with the DTS


## Entanglement
### Story
- **Priority** (A,B,C): 
- **As** Developer
- **I want to** there to be a linkage between the OS and the DT (uni- or bidirectional) 
- **So that I can*** provide close to reale time data to Services
### Acceptance Criteria
- **Given** that I can represent the properties within a model
- **When** configuring DTS at runtime
- **Then** I want there to be a linkage, which is timely defined and establish relations


## Composability
### Story
- **Priority** (A,B,C): 
- **As** Engineer/Operator/Deployer
- **I want to** have a composable DT
- **So that I can*** group DTS, to observe (and control) them as a group or individually
### Acceptance Criteria
- **Given** integration and composability is widely supported
- **When** grouping DTS together into a bigger entity
- **Then** all the DTS representing parts of a larger entity can be represented, considered, and interacted with as a single DTS according to the needs of the applications.


## Representativeness 
### Story
- **Priority** (A,B,C): 
- **As** Operator
- **I want to** store and represent all the present and past data relevant for the OS
- **So that I can*** analyze a change of the OS while considering past states
### Acceptance Criteria
- **Given** that the representation of the data is complete (enough) 
- **When** the OS changes 
- **Then** all for the DTS meaningful features of this change will be stored in a model


## Heartbeat
### Story
- **Priority** (A,B,C): 
- **As** Developer
- **I want to** I want the DTS to know weather one of the Twins is in operation or not
- **So that I can*** notify services to pause their task while tis situation lasts
### Acceptance Criteria
- **Given** that SOME-CONTEXT
- **When** SOME_ACTION_IS_CARRIED_OUT
- **Then** SOME_OBSERVABLE_OUTCOMES_SHOULD_OCCUR