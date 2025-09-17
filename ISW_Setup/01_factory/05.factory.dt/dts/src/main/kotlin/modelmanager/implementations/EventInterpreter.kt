package dts.modelmanager.implementations

import dts.modelmanager.components.IEventInterpreter
import dts.modelmanager.components.IQuery

class EventInterpreter(override var iQuery: IQuery) :IEventInterpreter {
}