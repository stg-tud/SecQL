package unisson.model.mock.vespucci

import de.tud.cs.st.vespucci.interfaces.{IEnsemble, IGlobalOutgoing}


/**
 *
 * Author: Ralf Mitschke
 * Date: 02.01.12
 * Time: 16:26
 *
 */
case class GlobalOutgoingConstraint(kind: String, source: IEnsemble, target: IEnsemble)
        extends IGlobalOutgoing
{
    source.getSourceConnections.add(this)

    target.getTargetConnections.add(this)

    def getDependencyKind = kind

    def getSource = source

    def getTarget = target

    override def toString = "GlobalOutgoingConstraint(" + source.getName + ", " + target.getName + ")"
}