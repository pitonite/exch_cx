package io.github.pitonite.exch_cx.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.pitonite.exch_cx.R
import io.github.pitonite.exch_cx.ui.theme.ExchTheme

@Composable
fun Notice(
    text: String,
    bgColor: Color = Color.Transparent,
    modifier: Modifier = Modifier,
) {
  Surface(
      modifier =
          modifier
              .border(2.dp, MaterialTheme.colorScheme.tertiary, CardDefaults.shape)
              .padding(dimensionResource(R.dimen.padding_lg)),
      shape = CardDefaults.shape,
      color = bgColor,
  ) {
    Text(text, textAlign = TextAlign.Center, fontSize = 18.sp, modifier = Modifier.fillMaxWidth())
  }
}

@Composable
fun Notice(
    modifier: Modifier = Modifier,
    bgColor: Color = Color.Transparent,
    borderColor: Color = MaterialTheme.colorScheme.tertiary,
    content: @Composable () -> Unit,
) {
  Surface(
      modifier =
          modifier
              .border(2.dp, borderColor, CardDefaults.shape)
              .padding(dimensionResource(R.dimen.padding_lg)),
      shape = CardDefaults.shape,
      color = bgColor,
  ) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = dimensionResource(R.dimen.padding_sm))
                .padding(
                    vertical = dimensionResource(R.dimen.padding_sm),
                ),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_md)),
    ) {
      content()
    }
  }
}

@Preview("default")
@Composable
fun NoticePreview() {
  ExchTheme() {
    Surface(Modifier.padding(10.dp)) {
      Surface(Modifier.padding(10.dp)) {
        Notice(
            text = stringResource(R.string.notice_etherium_based_coins),
        )
      }
    }
  }
}
