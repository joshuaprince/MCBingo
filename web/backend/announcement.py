from backend.models import Color


def announcement_board_marked(player: str, to_state: Color, goal_text: str):
    mark_type = 'invalidate' if to_state == Color.INVALIDATED else 'complete'
    return {
        'formatted': {
            'key': f'bingo.message.marking.{mark_type}',
            'player': player,
            'goal': goal_text,
        }
    }
