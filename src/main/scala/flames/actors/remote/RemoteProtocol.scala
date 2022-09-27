package flames.actors.remote

import flames.actors.path.ActorSelector

enum RemoteProtocol {
  case SelectionRequest(query: Vector[ActorSelector])
  case SelectionResponse(???)
}
